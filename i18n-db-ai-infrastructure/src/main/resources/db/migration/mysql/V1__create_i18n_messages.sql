-- V1__create_i18n_messages.sql  (MySQL)
-- ============================================================
-- Column names matched to I18nMessage.java's @Column mappings:
--   code, locale, message, tenant_id (entity) — was message_code/message_value, missing tenant_id.
-- ID strategy: Hibernate SequenceStyleGenerator emulates SEQUENCE via a
-- table-based generator on MySQL — i18n_messages_seq below provides that,
-- so the entity's @GeneratedValue(strategy = SEQUENCE) works unmodified.
-- ============================================================

CREATE TABLE IF NOT EXISTS i18n_messages (
                                             id            BIGINT           NOT NULL,
                                             code          VARCHAR(255)     NOT NULL,
    locale        VARCHAR(20)      NOT NULL,
    message       LONGTEXT         NOT NULL,
    tenant_id     VARCHAR(100)     NULL,
    source        ENUM('DB','PROPERTIES','AI')
    NOT NULL,
    ai_generated  TINYINT(1)       NOT NULL DEFAULT 0,
    created_at    DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
    ON UPDATE CURRENT_TIMESTAMP(3),

    CONSTRAINT pk_i18n_messages PRIMARY KEY (id),
    CONSTRAINT uq_i18n_code_locale UNIQUE (code, locale)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_i18n_messages_code   ON i18n_messages (code);
CREATE INDEX idx_i18n_messages_locale ON i18n_messages (locale);

-- ── Sequence emulation table for Hibernate SequenceStyleGenerator ──────────
-- Hibernate looks for a table matching the @SequenceGenerator's
-- sequenceName ("i18n_messages_seq") with a "next_val" column when the
-- dialect (MySQL) doesn't support native sequences.
CREATE TABLE IF NOT EXISTS i18n_messages_seq (
                                                 next_val BIGINT NOT NULL
) ENGINE=InnoDB;

INSERT INTO i18n_messages_seq (next_val) VALUES (1);