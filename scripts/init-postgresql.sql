-- JobPlus 数据库初始化脚本 (PostgreSQL)
-- 执行前请确保已连接到 PostgreSQL

-- 创建数据库（如果不存在）
SELECT 'CREATE DATABASE jobplus'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'jobplus')\gexec

\c jobplus;

-- ── 用户表 ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS t_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    real_name     VARCHAR(100),
    phone         VARCHAR(20),
    email         VARCHAR(100),
    role          VARCHAR(20) NOT NULL DEFAULT 'SEEKER' CHECK (role IN ('SEEKER','HR','ADMIN')),
    company_id    BIGINT,
    avatar        VARCHAR(500),
    status        SMALLINT DEFAULT 1,
    create_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_username ON t_user(username);
CREATE INDEX IF NOT EXISTS idx_role ON t_user(role);
CREATE INDEX IF NOT EXISTS idx_company ON t_user(company_id);

COMMENT ON TABLE t_user IS '用户表（求职者/HR/管理员）';
COMMENT ON COLUMN t_user.username IS '用户名/学号';
COMMENT ON COLUMN t_user.password_hash IS 'BCrypt加密密码';
COMMENT ON COLUMN t_user.real_name IS '真实姓名';
COMMENT ON COLUMN t_user.phone IS '手机号';
COMMENT ON COLUMN t_user.role IS '角色';
COMMENT ON COLUMN t_user.company_id IS '所属企业ID（HR）';
COMMENT ON COLUMN t_user.avatar IS '头像URL';
COMMENT ON COLUMN t_user.status IS '状态：1正常 0禁用';

-- ── 企业表（简化）────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS t_company (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    industry    VARCHAR(100),
    scale       VARCHAR(50),
    description TEXT,
    logo_url    VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_company IS '企业表';
COMMENT ON COLUMN t_company.name IS '企业名称';
COMMENT ON COLUMN t_company.industry IS '所属行业';
COMMENT ON COLUMN t_company.scale IS '规模';
COMMENT ON COLUMN t_company.description IS '简介';
COMMENT ON COLUMN t_company.logo_url IS 'Logo';

-- ── 职位表 ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS t_job (
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT NOT NULL,
    hr_user_id     BIGINT NOT NULL,
    title          VARCHAR(200) NOT NULL,
    salary_min     INT,
    salary_max     INT,
    salary_type    VARCHAR(20) DEFAULT 'monthly' CHECK (salary_type IN ('monthly','daily','hourly')),
    location       VARCHAR(200),
    work_type      VARCHAR(20) DEFAULT 'onsite' CHECK (work_type IN ('remote','onsite','hybrid')),
    requirements   TEXT,
    description    TEXT,
    tags           VARCHAR(500),
    status         SMALLINT DEFAULT 1,
    view_count     INT DEFAULT 0,
    apply_count    INT DEFAULT 0,
    create_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_company ON t_job(company_id);
CREATE INDEX IF NOT EXISTS idx_hr ON t_job(hr_user_id);
CREATE INDEX IF NOT EXISTS idx_status ON t_job(status);
CREATE INDEX IF NOT EXISTS idx_location ON t_job(location);

COMMENT ON TABLE t_job IS '职位表';
COMMENT ON COLUMN t_job.company_id IS '所属企业';
COMMENT ON COLUMN t_job.hr_user_id IS '发布HR的userId';
COMMENT ON COLUMN t_job.title IS '职位名称';
COMMENT ON COLUMN t_job.salary_min IS '最低薪资（月/元）';
COMMENT ON COLUMN t_job.salary_max IS '最高薪资';
COMMENT ON COLUMN t_job.salary_type IS '薪资类型';
COMMENT ON COLUMN t_job.location IS '工作地点';
COMMENT ON COLUMN t_job.work_type IS '工作方式';
COMMENT ON COLUMN t_job.requirements IS '岗位要求';
COMMENT ON COLUMN t_job.description IS '职位描述';
COMMENT ON COLUMN t_job.tags IS '标签，逗号分隔';
COMMENT ON COLUMN t_job.status IS '状态：1招聘中 0已关闭';
COMMENT ON COLUMN t_job.view_count IS '浏览次数';
COMMENT ON COLUMN t_job.apply_count IS '投递次数';

-- ── 简历表 ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS t_resume (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL UNIQUE,
    real_name     VARCHAR(100),
    gender        VARCHAR(10) CHECK (gender IN ('male','female','other')),
    age           INT,
    education     VARCHAR(50),
    major         VARCHAR(100),
    phone         VARCHAR(20),
    email         VARCHAR(100),
    work_exp      INT DEFAULT 0,
    skills        VARCHAR(500),
    content_json  JSONB,
    file_url      VARCHAR(500),
    visibility    SMALLINT DEFAULT 1,
    create_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user ON t_resume(user_id);
CREATE INDEX IF NOT EXISTS idx_visibility ON t_resume(visibility);

COMMENT ON TABLE t_resume IS '简历表';
COMMENT ON COLUMN t_resume.user_id IS '所属用户（1:1）';
COMMENT ON COLUMN t_resume.education IS '学历';
COMMENT ON COLUMN t_resume.major IS '专业';
COMMENT ON COLUMN t_resume.work_exp IS '工作年限';
COMMENT ON COLUMN t_resume.skills IS '技能，逗号分隔';
COMMENT ON COLUMN t_resume.content_json IS '简历内容JSON';
COMMENT ON COLUMN t_resume.file_url IS '附件URL（MinIO）';
COMMENT ON COLUMN t_resume.visibility IS '可见性：1公开 0私密';

-- ── 投递记录表 ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS t_delivery (
    id          BIGSERIAL PRIMARY KEY,
    job_id      BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    resume_id   BIGINT,
    status      SMALLINT DEFAULT 1,
    feedback    TEXT,
    apply_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_job_user UNIQUE (job_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_user ON t_delivery(user_id);
CREATE INDEX IF NOT EXISTS idx_job ON t_delivery(job_id);
CREATE INDEX IF NOT EXISTS idx_status ON t_delivery(status);
CREATE INDEX IF NOT EXISTS idx_apply_time ON t_delivery(apply_time);

COMMENT ON TABLE t_delivery IS '投递记录表';
COMMENT ON COLUMN t_delivery.job_id IS '投递的职位';
COMMENT ON COLUMN t_delivery.user_id IS '投递者';
COMMENT ON COLUMN t_delivery.resume_id IS '所投简历';
COMMENT ON COLUMN t_delivery.status IS '状态：1已投递 2待查看 3笔试中 4入职 5已拒绝 6已撤回';
COMMENT ON COLUMN t_delivery.feedback IS 'HR反馈';
COMMENT ON COLUMN t_delivery.apply_time IS '投递时间';

-- ── 创建更新时间触发器函数 ──────────────────────────────────────
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- ── 为各表添加更新时间触发器 ────────────────────────────────────
DROP TRIGGER IF EXISTS update_t_user_modtime ON t_user;
CREATE TRIGGER update_t_user_modtime BEFORE UPDATE ON t_user
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

DROP TRIGGER IF EXISTS update_t_job_modtime ON t_job;
CREATE TRIGGER update_t_job_modtime BEFORE UPDATE ON t_job
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

DROP TRIGGER IF EXISTS update_t_resume_modtime ON t_resume;
CREATE TRIGGER update_t_resume_modtime BEFORE UPDATE ON t_resume
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

DROP TRIGGER IF EXISTS update_t_delivery_modtime ON t_delivery;
CREATE TRIGGER update_t_delivery_modtime BEFORE UPDATE ON t_delivery
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ── 初始化演示数据 ───────────────────────────────────────────────
INSERT INTO t_company (name, industry, scale, description) VALUES
('星辰科技', '互联网', '100-499人', '专注AI驱动的企业服务SaaS平台')
ON CONFLICT DO NOTHING;

INSERT INTO t_user (username, password_hash, real_name, phone, email, role, company_id) VALUES
-- BCrypt加密的密码 "123456"
('seeker001', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IxqQFLKLKjR1gK.QVLBvNTHbHx3yW2', '张三', '13800001001', 'zhangsan@example.com', 'SEEKER', NULL),
('seeker002', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IxqQFLKLKjR1gK.QVLBvNTHbHx3yW2', '李四', '13800001002', 'lisi@example.com', 'SEEKER', NULL),
('hr001',     '$2a$10$N9qo8uLOickgx2ZMRZoMye.IxqQFLKLKjR1gK.QVLBvNTHbHx3yW2', '王HR',  '13800001003', 'wanghr@star.com',   'HR',     1),
('admin',     '$2a$10$N9qo8uLOickgx2ZMRZoMye.IxqQFLKLKjR1gK.QVLBvNTHbHx3yW2', '管理员', '13800000000', 'admin@jobplus.com',  'ADMIN',  NULL)
ON CONFLICT (username) DO NOTHING;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_job_title_company'
    ) THEN
        ALTER TABLE t_job
            ADD CONSTRAINT uk_job_title_company UNIQUE (title, company_id);
    END IF;
END $$;

INSERT INTO t_job (company_id, hr_user_id, title, salary_min, salary_max, location, work_type, requirements, description, tags, status) VALUES
(1, 3, '后端开发实习生', 200, 300, '北京·海淀区', 'onsite', '熟悉Java/Python，了解Spring Boot；本科在读及以上', '参与公司核心后端服务开发，与团队协作完成需求。', 'Java,SpringBoot,MySQL,实习', 1),
(1, 3, '前端开发工程师', 8000, 15000, '北京·海淀区', 'hybrid', '熟练Vue3或React，掌握TypeScript，了解前端工程化', '负责Web端页面开发，配合后端联调，负责前端性能优化。', 'Vue3,TypeScript,Vite,前端', 1),
(1, 3, '产品经理', 15000, 25000, '北京', 'onsite', '2年以上互联网产品经验，擅长需求分析和产品设计', '主导产品规划，对接研发和运营，推动产品迭代上线。', '产品设计,需求分析,PRD', 1)
ON CONFLICT (title, company_id) DO NOTHING;

INSERT INTO t_resume (user_id, real_name, gender, age, education, major, phone, email, work_exp, skills, content_json, visibility) VALUES
(1, '张三', 'male', 22, '本科在读', '计算机科学与技术', '13800001001', 'zhangsan@example.com', 0, 'Java,Python,Spring Boot,MySQL', '{"summary":"大三学生，热爱后端开发","projects":[{"name":"校园二手平台","desc":"使用Spring Boot开发的全栈项目"}]}'::jsonb, 1),
(2, '李四', 'male', 23, '本科', '软件工程', '13800001002', 'lisi@example.com', 1, 'Vue3,React,TypeScript,Node.js', '{"summary":"一年前端经验，熟悉Vue生态","projects":[{"name":"企业官网","desc":"使用Vue3+TailwindCSS开发"}]}'::jsonb, 1)
ON CONFLICT (user_id) DO NOTHING;
