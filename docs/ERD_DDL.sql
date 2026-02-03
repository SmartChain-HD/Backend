-- SmartChain ESG Platform DDL
-- ERDCloud Import용 PostgreSQL DDL

-- ==================== USER DOMAIN ====================

CREATE TABLE industry (
    industry_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    code VARCHAR(50)
);

CREATE TABLE company (
    company_id BIGSERIAL PRIMARY KEY,
    industry_id BIGINT REFERENCES industry(industry_id),
    name VARCHAR(255) NOT NULL,
    scale VARCHAR(50),
    sales BIGINT,
    asset BIGINT,
    business_number VARCHAR(50) UNIQUE,
    company_type VARCHAR(20),
    ceo_name VARCHAR(100),
    address VARCHAR(500),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN company.company_type IS 'TIER1, TIER2';
COMMENT ON COLUMN company.status IS 'ACTIVE, INACTIVE';

CREATE TABLE domain (
    domain_id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN domain.code IS 'ESG, SAFETY, COMPLIANCE';

CREATE TABLE role (
    role_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON COLUMN role.code IS 'GUEST, DRAFTER, APPROVER, REVIEWER';

CREATE TABLE "user" (
    user_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT REFERENCES company(company_id),
    role_id BIGINT REFERENCES role(role_id),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    user_password VARCHAR(255) NOT NULL,
    last_login_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN "user".status IS 'ACTIVE, INACTIVE, SUSPENDED';

CREATE TABLE user_domain_role (
    user_domain_role_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(user_id),
    domain_id BIGINT NOT NULL REFERENCES domain(domain_id),
    role_id BIGINT NOT NULL REFERENCES role(role_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_domain UNIQUE (user_id, domain_id)
);

CREATE TABLE role_request (
    request_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES "user"(user_id),
    company_id BIGINT REFERENCES company(company_id),
    domain_id BIGINT REFERENCES domain(domain_id),
    processed_by BIGINT REFERENCES "user"(user_id),
    requested_role VARCHAR(50),
    reason VARCHAR(500),
    status VARCHAR(20),
    reject_reason VARCHAR(500),
    decided_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN role_request.status IS 'PENDING, APPROVED, REJECTED';

-- ==================== DIAGNOSTIC DOMAIN ====================

CREATE TABLE category (
    category_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100)
);

COMMENT ON COLUMN category.name IS 'Environment, Social, Governance';

CREATE TABLE question (
    question_id BIGSERIAL PRIMARY KEY,
    industry_id BIGINT REFERENCES industry(industry_id),
    category_id BIGINT REFERENCES category(category_id),
    content TEXT,
    question_type VARCHAR(20),
    input_type VARCHAR(20),
    is_active BOOLEAN,
    unit VARCHAR(50)
);

COMMENT ON COLUMN question.question_type IS 'QUAL, QUANT';
COMMENT ON COLUMN question.input_type IS 'TEXT, RADIO, FILE';

CREATE TABLE campaign (
    campaign_id BIGSERIAL PRIMARY KEY,
    domain_id BIGINT REFERENCES domain(domain_id),
    campaign_code VARCHAR(100) UNIQUE,
    owner_company_id BIGINT,
    title VARCHAR(255),
    content TEXT,
    period_start_date DATE,
    period_end_date DATE,
    deadline DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE diagnostic (
    diagnostic_id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT REFERENCES campaign(campaign_id),
    company_id BIGINT REFERENCES company(company_id),
    domain_id BIGINT REFERENCES domain(domain_id),
    diagnostic_code VARCHAR(100) UNIQUE,
    title VARCHAR(255),
    drafter_id BIGINT,
    approver_id BIGINT,
    reviewer_id BIGINT,
    status VARCHAR(20) NOT NULL,
    qualitative_progress INT DEFAULT 0,
    quantitative_progress INT DEFAULT 0,
    overall_score INT,
    period_start_date DATE,
    period_end_date DATE,
    deadline DATE,
    submitted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN diagnostic.status IS 'WRITING, SUBMITTED, RETURNED, APPROVED, REVIEWING, COMPLETED';

CREATE TABLE result_qual (
    result_qual_id BIGSERIAL PRIMARY KEY,
    diagnostic_id BIGINT REFERENCES diagnostic(diagnostic_id),
    question_id BIGINT REFERENCES question(question_id),
    answer BOOLEAN
);

CREATE TABLE result_quant (
    result_id BIGSERIAL PRIMARY KEY,
    diagnostic_id BIGINT REFERENCES diagnostic(diagnostic_id),
    question_id BIGINT REFERENCES question(question_id),
    val_num DECIMAL(20,4),
    val_text VARCHAR(500)
);

CREATE TABLE diagnostic_history (
    history_id BIGSERIAL PRIMARY KEY,
    diagnostic_id BIGINT NOT NULL REFERENCES diagnostic(diagnostic_id),
    actor_id BIGINT REFERENCES "user"(user_id),
    action VARCHAR(50),
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    comment VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN diagnostic_history.action IS 'CREATED, SUBMITTED, APPROVED, REJECTED';

CREATE TABLE report (
    report_id BIGSERIAL PRIMARY KEY,
    diagnostic_id BIGINT UNIQUE REFERENCES diagnostic(diagnostic_id),
    file_path VARCHAR(500),
    status VARCHAR(50),
    risk_level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN report.risk_level IS 'HIGH, MEDIUM, LOW';

CREATE TABLE data_package (
    data_package_id BIGSERIAL PRIMARY KEY,
    diagnostic_id BIGINT,
    created_by BIGINT,
    file_path VARCHAR(500),
    manifest JSON,
    file_size BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==================== EVIDENCE DOMAIN ====================

CREATE TABLE evidence_file (
    result_file_id BIGSERIAL PRIMARY KEY,
    result_id BIGINT REFERENCES result_quant(result_id),
    diagnostic_id BIGINT REFERENCES diagnostic(diagnostic_id),
    file_path VARCHAR(500),
    original_file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    parsing_status VARCHAR(20),
    val_num DECIMAL(20,4),
    val_text VARCHAR(500),
    parsing_meta_info TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN evidence_file.parsing_status IS 'WAITING, PROCESSING, SUCCESS, FAILED';

-- ==================== APPROVAL DOMAIN ====================

CREATE TABLE approval (
    approval_id BIGSERIAL PRIMARY KEY,
    diagnostic_id BIGINT NOT NULL REFERENCES diagnostic(diagnostic_id),
    requester_id BIGINT NOT NULL REFERENCES "user"(user_id),
    approver_id BIGINT REFERENCES "user"(user_id),
    status VARCHAR(20) NOT NULL,
    request_comment VARCHAR(500),
    approver_comment VARCHAR(500),
    deadline DATE,
    processed_at TIMESTAMP,
    submitted_to_reviewer_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN approval.status IS 'WAITING, APPROVED, REJECTED';

-- ==================== REVIEW DOMAIN ====================

CREATE TABLE review (
    review_id BIGSERIAL PRIMARY KEY,
    diagnostic_id BIGINT NOT NULL REFERENCES diagnostic(diagnostic_id),
    company_id BIGINT NOT NULL REFERENCES company(company_id),
    assigned_reviewer_id BIGINT REFERENCES "user"(user_id),
    domain_id BIGINT REFERENCES domain(domain_id),
    processed_by_id BIGINT REFERENCES "user"(user_id),
    status VARCHAR(20) NOT NULL,
    score INT,
    risk_level VARCHAR(20),
    submitted_at TIMESTAMP,
    processed_at TIMESTAMP,
    comment VARCHAR(1000),
    category_comment_e VARCHAR(500),
    category_comment_s VARCHAR(500),
    category_comment_g VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN review.status IS 'REVIEWING, APPROVED, REVISION_REQUIRED';
COMMENT ON COLUMN review.risk_level IS 'LOW, MEDIUM, HIGH';

-- ==================== AI DOMAIN ====================

CREATE TABLE ai_analysis_result (
    id BIGSERIAL PRIMARY KEY,
    diagnostic_id BIGINT NOT NULL REFERENCES diagnostic(diagnostic_id),
    domain_code VARCHAR(20) NOT NULL,
    package_id VARCHAR(100),
    risk_level VARCHAR(20),
    verdict VARCHAR(20) NOT NULL,
    why_summary VARCHAR(500),
    result_json TEXT,
    analyzed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN ai_analysis_result.domain_code IS 'ESG, SAFETY, COMPLIANCE';
COMMENT ON COLUMN ai_analysis_result.risk_level IS 'LOW, MEDIUM, HIGH';
COMMENT ON COLUMN ai_analysis_result.verdict IS 'PASS, WARN, NEED_CLARIFY, NEED_FIX';

-- ==================== JOB DOMAIN ====================

CREATE TABLE async_job (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(100) NOT NULL UNIQUE,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    progress INT DEFAULT 0,
    message VARCHAR(500),
    requester_id BIGINT,
    target_id BIGINT,
    request_payload VARCHAR(2000),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    estimated_completion_at TIMESTAMP,
    result_url VARCHAR(1000),
    error_code VARCHAR(500),
    error_message VARCHAR(1000),
    retryable BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN async_job.job_type IS 'FILE_PARSING, REPORT_GENERATION, BULK_REPORT, EXPORT, AI_ESG_ANALYSIS, AI_COMPLIANCE_ANALYSIS, AI_SAFETY_ANALYSIS';
COMMENT ON COLUMN async_job.status IS 'PENDING, RUNNING, SUCCEEDED, FAILED';

-- ==================== NOTIFICATION DOMAIN ====================

CREATE TABLE notification (
    notification_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(user_id),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    link_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN notification.type IS 'APPROVAL_REQUEST, APPROVAL_RESULT, REVIEW_REQUEST, REVIEW_RESULT, SYSTEM';

-- ==================== LOG DOMAIN ====================

CREATE TABLE activity_log (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES "user"(user_id),
    action VARCHAR(50) NOT NULL,
    status VARCHAR(50),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    target_type VARCHAR(50),
    target_id VARCHAR(100),
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN activity_log.action IS 'LOGIN, LOGOUT, FILE_UPLOAD, FILE_DOWNLOAD, DIAGNOSTIC_CREATE, DIAGNOSTIC_SUBMIT';
COMMENT ON COLUMN activity_log.target_type IS 'DIAGNOSTIC, FILE, USER, APPROVAL, REVIEW';

-- ==================== AUTH DOMAIN ====================

CREATE TABLE email_verification_code (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
