-- V1__create_i18n_messages.sql  (Oracle 12c+)
-- ============================================================
-- Oracle-specific DDL.
-- GENERATED ALWAYS AS IDENTITY requires Oracle 12c+.
-- CLOB for message_value; NUMBER(1) for boolean (0/1).
-- updated_at trigger maintained by Hibernate @UpdateTimestamp — no DB trigger needed.
-- ============================================================

CREATE TABLE i18n_messages (
    id            NUMBER          GENERATED ALWAYS AS IDENTITY
                                  CONSTRAINT pk_i18n_messages PRIMARY KEY,
    message_code  VARCHAR2(255)   NOT NULL,
    locale        VARCHAR2(20)    NOT NULL,
    message_value CLOB            NOT NULL,
    source        VARCHAR2(20)    NOT NULL,   -- DB | PROPERTIES | AI
    ai_generated  NUMBER(1)       DEFAULT 0   NOT NULL,
    created_at    TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at    TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT uq_i18n_messages_code_loc UNIQUE (message_code, locale),
    CONSTRAINT chk_i18n_source CHECK (source IN ('DB', 'PROPERTIES', 'AI')),
    CONSTRAINT chk_i18n_ai_gen CHECK (ai_generated IN (0, 1))
);

CREATE INDEX idx_i18n_messages_code   ON i18n_messages (message_code);
CREATE INDEX idx_i18n_messages_locale ON i18n_messages (locale);
