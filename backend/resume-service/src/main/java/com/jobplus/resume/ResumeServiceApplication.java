package com.jobplus.resume;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(
        scanBasePackages = {"com.jobplus.resume", "com.jobplus.common"},
        exclude = {DataSourceAutoConfiguration.class}
)
@EnableDiscoveryClient
public class ResumeServiceApplication {
    public static void main(String[] args) { SpringApplication.run(ResumeServiceApplication.class, args); }
}
