package com.jobplus.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jobplus.database.active", havingValue = "sqlite")
public class SqliteSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("PRAGMA foreign_keys = ON");
        createUserTable();
        createCompanyTable();
        createJobTable();
        createResumeTable();
        createDeliveryTable();
        createOutboxTable();
        seedData();
        log.warn("JobPlus is using SQLite fallback database because PostgreSQL was unavailable.");
    }

    private void createUserTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_user (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    real_name TEXT,
                    phone TEXT,
                    email TEXT,
                    role TEXT NOT NULL DEFAULT 'SEEKER',
                    company_id INTEGER,
                    avatar TEXT,
                    status INTEGER DEFAULT 1,
                    create_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_user_username ON t_user(username)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_user_role ON t_user(role)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_user_company ON t_user(company_id)");
    }

    private void createCompanyTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_company (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    industry TEXT,
                    scale TEXT,
                    description TEXT,
                    logo_url TEXT,
                    create_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    private void createJobTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_job (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    company_id INTEGER NOT NULL,
                    hr_user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    salary_min INTEGER,
                    salary_max INTEGER,
                    salary_type TEXT DEFAULT 'monthly',
                    location TEXT,
                    work_type TEXT DEFAULT 'onsite',
                    requirements TEXT,
                    description TEXT,
                    tags TEXT,
                    status INTEGER DEFAULT 1,
                    view_count INTEGER DEFAULT 0,
                    apply_count INTEGER DEFAULT 0,
                    create_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (title, company_id)
                )
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_job_company ON t_job(company_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_job_hr ON t_job(hr_user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_job_status ON t_job(status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_job_location ON t_job(location)");
    }

    private void createResumeTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_resume (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL UNIQUE,
                    real_name TEXT,
                    gender TEXT,
                    age INTEGER,
                    education TEXT,
                    major TEXT,
                    phone TEXT,
                    email TEXT,
                    work_exp INTEGER DEFAULT 0,
                    skills TEXT,
                    content_json TEXT,
                    file_url TEXT,
                    visibility INTEGER DEFAULT 1,
                    create_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_resume_user ON t_resume(user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_resume_visibility ON t_resume(visibility)");
    }

    private void createDeliveryTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_delivery (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    job_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    resume_id INTEGER,
                    status INTEGER DEFAULT 1,
                    feedback TEXT,
                    apply_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (job_id, user_id)
                )
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_delivery_user ON t_delivery(user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_delivery_job ON t_delivery(job_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_delivery_status ON t_delivery(status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_delivery_apply_time ON t_delivery(apply_time)");
    }

    private void createOutboxTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_outbox_event (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    event_type TEXT NOT NULL,
                    aggregate_type TEXT NOT NULL,
                    aggregate_id INTEGER NOT NULL,
                    topic TEXT NOT NULL,
                    payload TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'NEW',
                    retry_count INTEGER NOT NULL DEFAULT 0,
                    next_retry_time TEXT DEFAULT CURRENT_TIMESTAMP,
                    create_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_outbox_status_retry ON t_outbox_event(status, next_retry_time)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_t_outbox_aggregate ON t_outbox_event(aggregate_type, aggregate_id)");
    }

    private void seedData() {
        jdbcTemplate.update("""
                INSERT OR IGNORE INTO t_company (id, name, industry, scale, description)
                VALUES (1, '星辰科技', '互联网', '100-499人', '专注AI驱动的企业服务SaaS平台')
                """);

        jdbcTemplate.update("""
                INSERT OR IGNORE INTO t_user (id, username, password_hash, real_name, phone, email, role, company_id)
                VALUES
                (1, 'seeker001', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IxqQFLKLKjR1gK.QVLBvNTHbHx3yW2', '张三', '13800001001', 'zhangsan@example.com', 'SEEKER', NULL),
                (2, 'seeker002', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IxqQFLKLKjR1gK.QVLBvNTHbHx3yW2', '李四', '13800001002', 'lisi@example.com', 'SEEKER', NULL),
                (3, 'hr001', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IxqQFLKLKjR1gK.QVLBvNTHbHx3yW2', '王HR', '13800001003', 'wanghr@star.com', 'HR', 1),
                (4, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IxqQFLKLKjR1gK.QVLBvNTHbHx3yW2', '管理员', '13800000000', 'admin@jobplus.com', 'ADMIN', NULL)
                """);

        jdbcTemplate.update("""
                INSERT OR IGNORE INTO t_job (id, company_id, hr_user_id, title, salary_min, salary_max, location, work_type, requirements, description, tags, status)
                VALUES
                (1, 1, 3, '后端开发实习生', 200, 300, '北京·海淀区', 'onsite', '熟悉Java/Python，了解Spring Boot；本科在读及以上', '参与公司核心后端服务开发，与团队协作完成需求。', 'Java,SpringBoot,MySQL,实习', 1),
                (2, 1, 3, '前端开发工程师', 8000, 15000, '北京·海淀区', 'hybrid', '熟练Vue3或React，掌握TypeScript，了解前端工程化', '负责Web端页面开发，配合后端联调，负责前端性能优化。', 'Vue3,TypeScript,Vite,前端', 1),
                (3, 1, 3, '产品经理', 15000, 25000, '北京', 'onsite', '2年以上互联网产品经验，擅长需求分析和产品设计', '主导产品规划，对接研发和运营，推动产品迭代上线。', '产品设计,需求分析,PRD', 1)
                """);

        jdbcTemplate.update("""
                INSERT OR IGNORE INTO t_resume (id, user_id, real_name, gender, age, education, major, phone, email, work_exp, skills, content_json, visibility)
                VALUES
                (1, 1, '张三', 'male', 22, '本科在读', '计算机科学与技术', '13800001001', 'zhangsan@example.com', 0, 'Java,Python,Spring Boot,MySQL', '{"summary":"大三学生，热爱后端开发","projects":[{"name":"校园二手平台","desc":"使用Spring Boot开发的全栈项目"}]}', 1),
                (2, 2, '李四', 'male', 23, '本科', '软件工程', '13800001002', 'lisi@example.com', 1, 'Vue3,React,TypeScript,Node.js', '{"summary":"一年前端经验，熟悉Vue生态","projects":[{"name":"企业官网","desc":"使用Vue3+TailwindCSS开发"}]}', 1)
                """);
    }
}
