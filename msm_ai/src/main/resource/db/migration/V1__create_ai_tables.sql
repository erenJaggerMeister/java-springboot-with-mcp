CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE SCHEMA IF NOT EXISTS msm_ai;

CREATE TABLE IF NOT EXISTS msm_ai.ai_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS msm_ai.mcp_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    url VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed initial Gemini AI Configuration
INSERT INTO msm_ai.ai_config (id, config_key, config_value, created_at, updated_at)
VALUES 
    ('a0000000-0000-0000-0000-000000000001', 'gemini_api_key', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('a0000000-0000-0000-0000-000000000002', 'gemini_model', 'gemini-2.5-flash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (config_key) DO NOTHING;

-- Seed initial MCP config pointing to msf_student
INSERT INTO msm_ai.mcp_config (id, name, url, is_active, created_at, updated_at)
VALUES 
    ('c0000000-0000-0000-0000-000000000001', 'msf-student-mcp', 'http://localhost:8084/api/msf_student/mcp', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
