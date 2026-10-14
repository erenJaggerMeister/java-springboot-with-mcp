CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login_id VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(100),
    created_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed default initial user 'marcellius@example.com' with role 'super_admin'
INSERT INTO users (id, login_id, password, name, role, active, created_by, created_at, updated_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'marcellius@example.com',
    '$2a$10$nprRJAKvMDGssDdjxG2nrOXcuGBfo6A4l9.JyBtsMOWatFQjFQ0Tu',
    'Marcellius',
    'super_admin',
    TRUE,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;
