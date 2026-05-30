package com.jobplus.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DatabaseFallbackEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String SOURCE_NAME = "jobplusDatabaseFallback";
    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    private static final String SQLITE_DRIVER = "org.sqlite.JDBC";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String primaryUrl = environment.getProperty("spring.datasource.url");
        if (!StringUtils.hasText(primaryUrl) || !primaryUrl.startsWith("jdbc:postgresql:")) {
            return;
        }

        boolean fallbackEnabled = environment.getProperty("jobplus.database.fallback.enabled", Boolean.class, true);
        if (!fallbackEnabled || canConnect(primaryUrl, environment)) {
            return;
        }

        String sqliteUrl = environment.getProperty(
                "jobplus.database.fallback.sqlite-url",
                "jdbc:sqlite:../data/jobplus-fallback.sqlite"
        );
        createSqliteParentDirectory(sqliteUrl);

        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", sqliteUrl);
        properties.put("spring.datasource.driver-class-name", SQLITE_DRIVER);
        properties.put("spring.datasource.username", "");
        properties.put("spring.datasource.password", "");
        properties.put("spring.datasource.hikari.maximum-pool-size", 1);
        properties.put("spring.datasource.hikari.minimum-idle", 1);
        properties.put("spring.datasource.hikari.connection-test-query", "SELECT 1");
        properties.put("jobplus.database.active", "sqlite");
        properties.put("jobplus.database.primary-url", primaryUrl);

        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, properties));
    }

    private boolean canConnect(String url, ConfigurableEnvironment environment) {
        String username = environment.getProperty("spring.datasource.username", "");
        String password = environment.getProperty("spring.datasource.password", "");
        int timeoutMs = environment.getProperty("jobplus.database.fallback.probe-timeout-ms", Integer.class, 1500);

        try {
            Class.forName(POSTGRES_DRIVER);
            DriverManager.setLoginTimeout((int) Math.ceil(Duration.ofMillis(timeoutMs).toSeconds()));
            try (var ignored = DriverManager.getConnection(url, username, password)) {
                return true;
            }
        } catch (Exception ex) {
            System.err.printf(
                    "PostgreSQL datasource unavailable, falling back to SQLite. url=%s reason=%s%n",
                    url,
                    ex.getMessage()
            );
            return false;
        }
    }

    private void createSqliteParentDirectory(String sqliteUrl) {
        String prefix = "jdbc:sqlite:";
        if (!sqliteUrl.startsWith(prefix)) {
            return;
        }
        String location = sqliteUrl.substring(prefix.length());
        if (!StringUtils.hasText(location) || ":memory:".equals(location)) {
            return;
        }

        try {
            Path parent = Path.of(location).toAbsolutePath().normalize().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception ex) {
            System.err.printf("Unable to create SQLite fallback directory for %s: %s%n", sqliteUrl, ex.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
