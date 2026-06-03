CREATE SCHEMA IF NOT EXISTS network_monitor;

SET search_path TO network_monitor;

CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    unique_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    location VARCHAR(255) NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE status_reports (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ONLINE', 'OFFLINE', 'DEGRADED')),
    message TEXT
);

CREATE INDEX idx_devices_unique_id
    ON devices(unique_id);

CREATE INDEX idx_status_reports_device_id_reported_at
    ON status_reports(device_id, reported_at DESC);
