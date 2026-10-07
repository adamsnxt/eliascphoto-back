CREATE TABLE consultation_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price_usd NUMERIC(12, 2) NOT NULL CHECK (price_usd > 0),
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE appointment_schedule_locks (
    id BIGINT PRIMARY KEY
);

INSERT INTO appointment_schedule_locks (id) VALUES (1);

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    instagram_handle VARCHAR(100) NOT NULL,
    consultation_type_id BIGINT NOT NULL REFERENCES consultation_types(id) ON DELETE RESTRICT,
    consultation_name_snapshot VARCHAR(100) NOT NULL,
    price_usd_snapshot NUMERIC(12, 2) NOT NULL CHECK (price_usd_snapshot > 0),
    duration_minutes_snapshot INTEGER NOT NULL CHECK (duration_minutes_snapshot > 0),
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(24) NOT NULL CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'CANCELLED')),
    paid_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    CHECK (end_at > start_at)
);

CREATE INDEX idx_appointments_start_at ON appointments(start_at);
CREATE INDEX idx_appointments_status_interval ON appointments(status, start_at, end_at);
CREATE INDEX idx_appointments_consultation_type ON appointments(consultation_type_id);