-- Workflow Service schema

-- workflow_definitions
CREATE TABLE workflow_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    entity_type VARCHAR(100) NOT NULL,
    trigger_event VARCHAR(100),
    steps_config JSONB DEFAULT '[]',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (code, tenant_id)
);

CREATE INDEX idx_workflow_definitions_tenant_id ON workflow_definitions (tenant_id);

-- workflow_steps
CREATE TABLE workflow_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    workflow_id UUID NOT NULL REFERENCES workflow_definitions(id) ON DELETE CASCADE,
    step_order INT NOT NULL,
    step_name VARCHAR(200) NOT NULL,
    step_type VARCHAR(30) NOT NULL DEFAULT 'APPROVAL',
    approver_type VARCHAR(30),
    approver_id UUID,
    approver_role VARCHAR(100),
    can_delegate BOOLEAN NOT NULL DEFAULT FALSE,
    sla_hours INT NOT NULL DEFAULT 24,
    escalation_to UUID,
    required BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_workflow_steps_workflow_id ON workflow_steps (workflow_id);
CREATE INDEX idx_workflow_steps_tenant_id ON workflow_steps (tenant_id);

-- workflow_instances
CREATE TABLE workflow_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    workflow_id UUID NOT NULL REFERENCES workflow_definitions(id) ON DELETE RESTRICT,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    initiated_by UUID NOT NULL,
    current_step_order INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    context_data JSONB DEFAULT '{}',
    completed_at TIMESTAMPTZ,
    notes TEXT
);

CREATE INDEX idx_workflow_instances_tenant_entity ON workflow_instances (tenant_id, entity_id);
CREATE INDEX idx_workflow_instances_tenant_initiator ON workflow_instances (tenant_id, initiated_by);
CREATE INDEX idx_workflow_instances_tenant_id ON workflow_instances (tenant_id);

-- delegation_rules
CREATE TABLE delegation_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    delegator_id UUID NOT NULL,
    delegate_id UUID NOT NULL,
    scope VARCHAR(30) NOT NULL DEFAULT 'ALL',
    entity_types JSONB DEFAULT '[]',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    reason VARCHAR(300)
);

CREATE INDEX idx_delegation_rules_tenant_delegator ON delegation_rules (tenant_id, delegator_id);
CREATE INDEX idx_delegation_rules_tenant_delegate ON delegation_rules (tenant_id, delegate_id);
CREATE INDEX idx_delegation_rules_tenant_id ON delegation_rules (tenant_id);
