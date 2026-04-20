-- ============================================================================
-- AETHER BANK - IAM SERVICE DATABASE SCHEMA
-- Production-Grade Security Architecture
-- ============================================================================

-- ============================================================================
-- ROLES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- PERMISSIONS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- ROLE_PERMISSIONS TABLE (Many-to-Many)
-- ============================================================================
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone_number VARCHAR(20),
    
    -- Account Status
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_email_verified BOOLEAN NOT NULL DEFAULT false,
    
    -- Account Locking (for brute force protection)
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_failed_login TIMESTAMP,
    locked_until TIMESTAMP,
    
    -- MFA
    mfa_enabled BOOLEAN NOT NULL DEFAULT false,
    mfa_secret VARCHAR(255),
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    
    CONSTRAINT valid_email CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

-- ============================================================================
-- USER_ROLES TABLE (Many-to-Many)
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_roles (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    role_id INT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID,
    
    UNIQUE(user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================================
-- REFRESH_TOKENS TABLE
-- For token rotation and revocation
-- ============================================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(512) NOT NULL UNIQUE,
    
    -- Token Details
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT false,
    revoked_at TIMESTAMP,
    
    -- Device Tracking
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================================
-- OTP_CODES TABLE
-- For email-based OTP and MFA backup codes
-- ============================================================================
CREATE TABLE IF NOT EXISTS otp_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    code VARCHAR(10) NOT NULL,
    
    -- OTP Purpose
    purpose VARCHAR(50) NOT NULL,  -- LOGIN, PASSWORD_RESET, EMAIL_VERIFICATION
    
    -- Validity
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT false,
    used_at TIMESTAMP,
    
    -- Attempts
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 3,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================================
-- AUDIT_LOGS TABLE
-- Comprehensive audit trail for security and compliance
-- ============================================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    
    -- Action Details
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(255),
    
    -- Change Details
    old_value TEXT,
    new_value TEXT,
    
    -- Request Context
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_method VARCHAR(10),
    request_path VARCHAR(500),
    
    -- Status
    status VARCHAR(50),
    status_code INT,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================================
-- SESSIONS TABLE
-- Optional: For tracking active user sessions
-- ============================================================================
CREATE TABLE IF NOT EXISTS sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    refresh_token_id UUID,
    
    -- Session Details
    device_type VARCHAR(50),
    browser VARCHAR(255),
    os_name VARCHAR(255),
    
    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (refresh_token_id) REFERENCES refresh_tokens(id) ON DELETE SET NULL
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- Users indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_locked_until ON users(locked_until);

-- User Roles indexes
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Refresh Tokens indexes
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_is_revoked ON refresh_tokens(is_revoked);

-- OTP Codes indexes
CREATE INDEX idx_otp_codes_user_id ON otp_codes(user_id);
CREATE INDEX idx_otp_codes_expires_at ON otp_codes(expires_at);
CREATE INDEX idx_otp_codes_purpose ON otp_codes(purpose);

-- Audit Logs indexes
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);

-- Sessions indexes
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_is_active ON sessions(is_active);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);

-- ============================================================================
-- DEFAULT ROLES
-- ============================================================================
INSERT INTO roles (name, description) VALUES 
    ('ADMIN', 'System administrator with full access'),
    ('EMPLOYEE', 'Bank employee with operational permissions'),
    ('CUSTOMER', 'Regular customer with basic banking permissions')
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- DEFAULT PERMISSIONS
-- ============================================================================
INSERT INTO permissions (name, description, resource, action) VALUES 
    -- User Management Permissions
    ('CREATE_USER', 'Create new user account', 'USER', 'CREATE'),
    ('READ_USER', 'View user details', 'USER', 'READ'),
    ('UPDATE_USER', 'Modify user information', 'USER', 'UPDATE'),
    ('DELETE_USER', 'Delete user account', 'USER', 'DELETE'),
    ('VIEW_ALL_USERS', 'View all users', 'USER', 'LIST'),
    
    -- Role Management Permissions
    ('MANAGE_ROLES', 'Create and manage roles', 'ROLE', 'MANAGE'),
    ('ASSIGN_ROLE', 'Assign roles to users', 'ROLE', 'ASSIGN'),
    
    -- Permission Management Permissions
    ('MANAGE_PERMISSIONS', 'Manage permissions', 'PERMISSION', 'MANAGE'),
    
    -- Audit Permissions
    ('VIEW_AUDIT_LOGS', 'Access audit logs', 'AUDIT', 'READ'),
    
    -- Account Permissions
    ('LOCK_ACCOUNT', 'Lock user account', 'ACCOUNT', 'LOCK'),
    ('UNLOCK_ACCOUNT', 'Unlock user account', 'ACCOUNT', 'UNLOCK'),
    
    -- Transaction Permissions
    ('VIEW_TRANSACTIONS', 'View transactions', 'TRANSACTION', 'READ'),
    ('CREATE_TRANSACTION', 'Create transaction', 'TRANSACTION', 'CREATE')
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- ASSIGN PERMISSIONS TO ROLES
-- ============================================================================

-- ADMIN: Full access
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- EMPLOYEE: Operational permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'EMPLOYEE' 
AND p.name IN ('READ_USER', 'VIEW_TRANSACTIONS', 'CREATE_TRANSACTION')
ON CONFLICT DO NOTHING;

-- CUSTOMER: Basic permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'CUSTOMER' 
AND p.name IN ('READ_USER', 'VIEW_TRANSACTIONS', 'CREATE_TRANSACTION')
ON CONFLICT DO NOTHING;

-- ============================================================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================================================

COMMENT ON TABLE users IS 'Core user accounts with security attributes';
COMMENT ON TABLE roles IS 'Role definitions for RBAC';
COMMENT ON TABLE permissions IS 'Fine-grained permissions';
COMMENT ON TABLE user_roles IS 'Many-to-many mapping of users to roles';
COMMENT ON TABLE refresh_tokens IS 'Token rotation and revocation tracking';
COMMENT ON TABLE otp_codes IS 'One-time passwords for MFA and email verification';
COMMENT ON TABLE audit_logs IS 'Comprehensive audit trail for compliance';
COMMENT ON TABLE sessions IS 'Active user sessions';

COMMENT ON COLUMN users.failed_login_attempts IS 'Counter for brute force detection (reset after successful login or lock timeout)';
COMMENT ON COLUMN users.locked_until IS 'Account lock expiration timestamp (account locked if current_time < locked_until)';
COMMENT ON COLUMN users.mfa_enabled IS 'True if user has enabled TOTP-based MFA';
COMMENT ON COLUMN users.mfa_secret IS 'Base32-encoded TOTP secret key';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-256 hash of refresh token (never store plaintext)';
COMMENT ON COLUMN otp_codes.purpose IS 'Enum: LOGIN, PASSWORD_RESET, EMAIL_VERIFICATION';
