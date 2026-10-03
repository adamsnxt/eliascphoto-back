CREATE TABLE refresh_token_sessions (
    token_id VARCHAR(36) PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL REFERENCES users(user_name) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_refresh_token_sessions_user_name ON refresh_token_sessions(user_name);
CREATE INDEX idx_refresh_token_sessions_expires_at ON refresh_token_sessions(expires_at);