-- V1__create_i18n_messages.sql  (common / H2 / ANSI SQL)
-- ============================================================
-- Creates the i18n_messages table using standard ANSI SQL.
-- Used for H2 (unit/integration tests) and as the generic fallback.
-- ============================================================

CREATE TABLE IF NOT EXISTS i18n_messages (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    message_code  VARCHAR(255)    NOT NULL,
    locale        VARCHAR(20)     NOT NULL,
    message_value TEXT            NOT NULL,
    source        VARCHAR(20)     NOT NULL,   -- DB | PROPERTIES | AI
    ai_generated  BOOLEAN         NOT NULL    DEFAULT FALSE,
    created_at    TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_i18n_messages          PRIMARY KEY (id),
    CONSTRAINT uq_i18n_messages_code_loc UNIQUE (message_code, locale)
);

CREATE INDEX IF NOT EXISTS idx_i18n_messages_code
    ON i18n_messages (message_code);

CREATE INDEX IF NOT EXISTS idx_i18n_messages_locale
    ON i18n_messages (locale);
