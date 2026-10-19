CREATE EXTENSION IF NOT EXISTS timescaledb;

SELECT create_hypertable('measurements', by_range('measured_at'));