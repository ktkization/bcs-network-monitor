ALTER TABLE network_monitor.devices
    ADD COLUMN last_report_at TIMESTAMP,
    ADD COLUMN current_status VARCHAR(20) CHECK (current_status IN ('ONLINE', 'OFFLINE', 'DEGRADED'));

CREATE INDEX idx_devices_last_report_at
    ON network_monitor.devices(last_report_at);

CREATE INDEX idx_devices_current_status
    ON network_monitor.devices(current_status);
