-- V1__create_i18n_messages.sql  (PostgreSQL)
-- ============================================================
-- PostgreSQL-specific DDL.
-- BIGSERIAL for auto-increment; TEXT for unlimited message values.
-- now() for timestamp defaults; updated_at managed by Hibernate @UpdateTimestamp.
-- ============================================================

CREATE TABLE IF NOT EXISTS i18n_messages (
    id            BIGSERIAL       NOT NULL,
    message_code  VARCHAR(255)    NOT NULL,
    locale        VARCHAR(20)     NOT NULL,
    message_value TEXT            NOT NULL,
    source        VARCHAR(20)     NOT NULL,   -- DB | PROPERTIES | AI
    ai_generated  BOOLEAN         NOT NULL    DEFAULT FALSE,
    created_at    TIMESTAMPTZ     NOT NULL    DEFAULT now(),
    updated_at    TIMESTAMPTZ     NOT NULL    DEFAULT now(),

    CONSTRAINT pk_i18n_messages          PRIMARY KEY (id),
    CONSTRAINT uq_i18n_messages_code_loc UNIQUE (message_code, locale),
    CONSTRAINT chk_i18n_source           CHECK (source IN ('DB', 'PROPERTIES', 'AI'))
);

CREATE INDEX IF NOT EXISTS idx_i18n_messages_code
    ON i18n_messages (message_code);

CREATE INDEX IF NOT EXISTS idx_i18n_messages_locale
    ON i18n_messages (locale);
