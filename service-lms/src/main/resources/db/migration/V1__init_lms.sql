-- ============================================================
-- LMS Service Schema
-- ============================================================

-- COURSES
CREATE TABLE courses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50)  NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,

    title           VARCHAR(300) NOT NULL,
    description     TEXT,
    category        VARCHAR(100),
    instructor_id   UUID,
    duration_hours  DECIMAL(6,2),
    level           VARCHAR(20)  NOT NULL DEFAULT 'BEGINNER',
    format          VARCHAR(20)  NOT NULL DEFAULT 'ONLINE',
    thumbnail_url   VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    tags            JSONB        NOT NULL DEFAULT '[]',
    mandatory       BOOLEAN      NOT NULL DEFAULT FALSE,
    target_roles    JSONB        NOT NULL DEFAULT '[]'
);

CREATE INDEX idx_courses_tenant_id   ON courses (tenant_id);
CREATE INDEX idx_courses_status      ON courses (tenant_id, status) WHERE is_deleted = FALSE;
CREATE INDEX idx_courses_mandatory   ON courses (tenant_id, mandatory) WHERE is_deleted = FALSE;

-- COURSE MODULES
CREATE TABLE course_modules (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50)  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,

    course_id        UUID         NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title            VARCHAR(300) NOT NULL,
    description      TEXT,
    order_index      INT          NOT NULL DEFAULT 0,
    duration_minutes INT,
    content_type     VARCHAR(30)  NOT NULL DEFAULT 'DOCUMENT',
    content_url      VARCHAR(500),
    status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
);

CREATE INDEX idx_course_modules_tenant_id ON course_modules (tenant_id);
CREATE INDEX idx_course_modules_course_id ON course_modules (course_id);

-- COURSE ENROLLMENTS
CREATE TABLE course_enrollments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50)   NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,

    course_id           UUID          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    employee_id         UUID          NOT NULL,
    enrolled_by         UUID,
    status              VARCHAR(20)   NOT NULL DEFAULT 'ENROLLED',
    progress_percentage DECIMAL(5,2)  NOT NULL DEFAULT 0,
    enrolled_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    due_date            DATE,
    score               DECIMAL(5,2)
);

CREATE UNIQUE INDEX idx_enrollments_unique_active
    ON course_enrollments (tenant_id, course_id, employee_id)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_course_enrollments_tenant_id  ON course_enrollments (tenant_id);
CREATE INDEX idx_course_enrollments_employee   ON course_enrollments (tenant_id, employee_id);

-- ASSESSMENTS
CREATE TABLE assessments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50)  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,

    course_id        UUID         NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title            VARCHAR(300) NOT NULL,
    description      TEXT,
    total_marks      INT          NOT NULL DEFAULT 100,
    passing_marks    INT          NOT NULL DEFAULT 60,
    duration_minutes INT,
    attempts_allowed INT          NOT NULL DEFAULT 3,
    status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    questions        JSONB        NOT NULL DEFAULT '[]'
);

CREATE INDEX idx_assessments_tenant_id ON assessments (tenant_id);
CREATE INDEX idx_assessments_course_id ON assessments (course_id);

-- CERTIFICATIONS
CREATE TABLE certifications (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50)  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,

    employee_id      UUID         NOT NULL,
    course_id        UUID         REFERENCES courses(id) ON DELETE SET NULL,
    certificate_name VARCHAR(300) NOT NULL,
    issued_by        VARCHAR(200),
    issue_date       DATE         NOT NULL,
    expiry_date      DATE,
    certificate_url  VARCHAR(500),
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX idx_certifications_tenant_id ON certifications (tenant_id);
CREATE INDEX idx_certifications_employee  ON certifications (tenant_id, employee_id);
