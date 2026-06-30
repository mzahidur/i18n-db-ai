-- V1__create_i18n_messages.sql  (SQL Server)
-- ============================================================
-- SQL Server-specific DDL (2016+).
-- IDENTITY(1,1) for auto-increment; NVARCHAR(MAX) for Unicode message values.
-- BIT for boolean; DATETIME2 for high-precision timestamps.
-- updated_at managed by Hibernate @UpdateTimestamp — no trigger needed.
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.tables WHERE name = 'i18n_messages'
)
BEGIN
    CREATE TABLE i18n_messages (
        id            BIGINT           NOT NULL IDENTITY(1,1),
        message_code  NVARCHAR(255)    NOT NULL,
        locale        NVARCHAR(20)     NOT NULL,
        message_value NVARCHAR(MAX)    NOT NULL,
        source        NVARCHAR(20)     NOT NULL,   -- DB | PROPERTIES | AI
        ai_generated  BIT              NOT NULL    DEFAULT 0,
        created_at    DATETIME2(3)     NOT NULL    DEFAULT SYSUTCDATETIME(),
        updated_at    DATETIME2(3)     NOT NULL    DEFAULT SYSUTCDATETIME(),

        CONSTRAINT pk_i18n_messages          PRIMARY KEY (id),
        CONSTRAINT uq_i18n_messages_code_loc UNIQUE (message_code, locale),
        CONSTRAINT chk_i18n_source           CHECK (source IN ('DB', 'PROPERTIES', 'AI'))
    );

    CREATE INDEX idx_i18n_messages_code   ON i18n_messages (message_code);
    CREATE INDEX idx_i18n_messages_locale ON i18n_messages (locale);
END;
