CREATE SCHEMA IF NOT EXISTS shared;
CREATE SCHEMA IF NOT EXISTS tenant_demo;
GRANT ALL ON SCHEMA shared TO hrms_user;
GRANT ALL ON SCHEMA tenant_demo TO hrms_user;

CREATE TABLE shared.tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL, slug VARCHAR(100) UNIQUE NOT NULL,
    schema_name VARCHAR(100) UNIQUE NOT NULL, status VARCHAR(20) DEFAULT 'ACTIVE',
    logo_url TEXT, primary_color VARCHAR(7) DEFAULT '#1890ff',
    timezone VARCHAR(50) DEFAULT 'Asia/Kolkata', currency VARCHAR(3) DEFAULT 'INR',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE shared.plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL, code VARCHAR(50) UNIQUE NOT NULL,
    max_employees INT NOT NULL, monthly_price DECIMAL(10,2) DEFAULT 0,
    features JSONB DEFAULT '{}', is_active BOOLEAN DEFAULT TRUE
);

INSERT INTO shared.plans (name,code,max_employees,monthly_price,features) VALUES
('Free','FREE',5,0,'{"modules":["core_hr","leave","attendance"]}'),
('Starter','STARTER',50,2999,'{"modules":["core_hr","leave","attendance","payroll"]}'),
('Professional','PRO',200,5999,'{"modules":"all"}'),
('Enterprise','ENTERPRISE',10000,9999,'{"modules":"all","sso":true}');

INSERT INTO shared.tenants (name,slug,schema_name) VALUES ('Demo Company','demo','tenant_demo');

CREATE TABLE shared.audit_logs (
    id BIGSERIAL PRIMARY KEY, tenant_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100), action VARCHAR(50) NOT NULL, module VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100), entity_id VARCHAR(100),
    old_value JSONB, new_value JSONB, ip_address VARCHAR(45), created_at TIMESTAMP DEFAULT NOW()
);
