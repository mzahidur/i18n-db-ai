-- V1__create_i18n_messages.sql  (MySQL)
-- ============================================================
-- MySQL-specific DDL.
-- ENGINE=InnoDB, CHARSET=utf8mb4 for full Unicode (emoji-safe).
-- AUTO_INCREMENT primary key; ON UPDATE CURRENT_TIMESTAMP handles updated_at.
-- ============================================================

CREATE TABLE IF NOT EXISTS i18n_messages (
    id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    message_code  VARCHAR(255)     NOT NULL,
    locale        VARCHAR(20)      NOT NULL,
    message_value LONGTEXT         NOT NULL,
    source        ENUM('DB','PROPERTIES','AI')
                                   NOT NULL,
    ai_generated  TINYINT(1)       NOT NULL DEFAULT 0,
    created_at    DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                            ON UPDATE CURRENT_TIMESTAMP(3),

    CONSTRAINT pk_i18n_messages          PRIMARY KEY (id),
    CONSTRAINT uq_i18n_messages_code_loc UNIQUE (message_code, locale)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_i18n_messages_code   ON i18n_messages (message_code);
CREATE INDEX idx_i18n_messages_locale ON i18n_messages (locale);
