-- V1__create_i18n_messages.sql (H2 specific)
-- Matches domain I18nMessage entity's GenerationType.SEQUENCE + named sequence

CREATE TABLE IF NOT EXISTS i18n_messages (
                                             id            BIGINT          NOT NULL,
                                             message_code  VARCHAR(255)    NOT NULL,
    locale        VARCHAR(20)     NOT NULL,
    message_value TEXT            NOT NULL,
    source        VARCHAR(20)     NOT NULL,   -- DB | PROPERTIES | AI
    ai_generated  BOOLEAN         NOT NULL    DEFAULT FALSE,
    created_at    TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_i18n_messages          PRIMARY KEY (id),
    CONSTRAINT uq_i18n_messages_code_loc UNIQUE (message_code, locale)
    );

CREATE SEQUENCE IF NOT EXISTS i18n_messages_seq
    START WITH 1
    INCREMENT BY 50
    MINVALUE 1
    NO MAXVALUE
    CACHE 20;

CREATE INDEX IF NOT EXISTS idx_i18n_messages_code
    ON i18n_messages (message_code);

CREATE INDEX IF NOT EXISTS idx_i18n_messages_locale
    ON i18n_messages (locale);