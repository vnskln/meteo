CREATE TABLE series (
    id         BIGINT            GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(100)      NOT NULL CHECK (char_length(name) >= 1),
    min_value  DOUBLE PRECISION  NOT NULL,
    max_value  DOUBLE PRECISION  NOT NULL,
    color      VARCHAR(7)        NOT NULL CHECK (color ~ '^#[0-9A-Fa-f]{6}$'),
    icon       VARCHAR(100),
    unit       VARCHAR(20),
    CONSTRAINT series_min_lt_max_chk CHECK (min_value < max_value)
);

CREATE TABLE sensors (
    id                   BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    series_id            BIGINT        NOT NULL REFERENCES series (id) ON DELETE CASCADE,
    name                 VARCHAR(100)  NOT NULL CHECK (char_length(name) >= 1),
    api_key_hash         BYTEA         UNIQUE NOT NULL,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    revoked_at           TIMESTAMPTZ
);

CREATE TABLE measurements (
    id           BIGINT            GENERATED ALWAYS AS IDENTITY,
    series_id    BIGINT            NOT NULL REFERENCES series (id) ON DELETE CASCADE,
    sensor_id    BIGINT            NOT NULL REFERENCES sensors (id),
    value        DOUBLE PRECISION  NOT NULL,
    measured_at  TIMESTAMPTZ       NOT NULL,
    PRIMARY KEY (id, measured_at)
);

CREATE INDEX measurements_series_id_measured_at_idx ON measurements (series_id, measured_at);

CREATE INDEX measurements_sensor_id_idx ON measurements (sensor_id);

CREATE TABLE admin_users (
    id             BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username       TEXT          UNIQUE NOT NULL,
    password_hash  VARCHAR(200)  NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE sessions (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES admin_users (id) ON DELETE CASCADE,
    token_hash  BYTEA        UNIQUE NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ  NOT NULL
);
