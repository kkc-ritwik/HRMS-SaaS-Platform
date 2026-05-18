CREATE TABLE skills (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100) NOT NULL,
    code        VARCHAR(50) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    category    VARCHAR(50),
    is_active   BOOLEAN,
    UNIQUE (tenant_id, code)
);

CREATE TABLE employee_skills (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          VARCHAR(100) NOT NULL,
    employee_id        UUID NOT NULL,
    skill_id           UUID NOT NULL REFERENCES skills(id),
    proficiency        INTEGER NOT NULL CHECK (proficiency BETWEEN 1 AND 5),
    years_of_experience DOUBLE PRECISION,
    self_rated         BOOLEAN,
    manager_endorsed   BOOLEAN,
    last_used          DATE,
    UNIQUE (tenant_id, employee_id, skill_id)
);

CREATE TABLE role_skill_requirements (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    designation_id  UUID NOT NULL,
    skill_id        UUID NOT NULL REFERENCES skills(id),
    min_proficiency INTEGER NOT NULL CHECK (min_proficiency BETWEEN 1 AND 5),
    is_mandatory    BOOLEAN,
    UNIQUE (tenant_id, designation_id, skill_id)
);
