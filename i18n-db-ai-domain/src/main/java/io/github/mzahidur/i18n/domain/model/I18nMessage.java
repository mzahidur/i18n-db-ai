package io.github.mzahidur.i18n.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;

import java.util.Objects;

/**
 * Core domain entity representing a single translated message.
 *
 * <p>Maps to the {@code i18n_messages} table. The schema is intentionally
 * database-agnostic:</p>
 * <ul>
 *   <li>Primary key uses {@link GenerationType#SEQUENCE} with a named sequence,
 *       which is portable across Oracle, PostgreSQL, and H2. Vendor-specific
 *       Flyway migration scripts handle the DDL per database dialect.</li>
 *   <li>Audit fields are embedded via {@link AuditMetadata} — no DB triggers
 *       required; lifecycle is managed by JPA {@code @PrePersist/@PreUpdate}.</li>
 * </ul>
 *
 * <p><strong>Domain rules enforced here:</strong></p>
 * <ul>
 *   <li>A {@code (code, locale)} pair is unique (enforced at DB and domain level).</li>
 *   <li>Neither {@code code} nor {@code locale} may be blank.</li>
 *   <li>Message content may not be null (empty string is allowed for intentional blanks).</li>
 * </ul>
 *
 * <p>This entity lives in the domain layer and carries only JPA annotations —
 * no Spring, Hibernate-specific, or framework annotations beyond the JPA spec.</p>
 */
@Entity
@Table(
    name = "i18n_messages",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_i18n_code_locale",
        columnNames = { "code", "locale" }
    )
)
@SequenceGenerator(
    name            = "i18n_seq",
    sequenceName    = "i18n_messages_seq",
    allocationSize  = 50   // batch allocation reduces DB round-trips
)
public class I18nMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "i18n_seq")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * Message key — e.g. {@code "user.welcome.title"}.
     * Mirrors the key used in Spring's {@code MessageSource}.
     */
    @Column(name = "code", nullable = false, length = 255)
    private String code;

    /**
     * BCP-47 locale tag — e.g. {@code "en"}, {@code "en-US"}, {@code "ms-MY"}.
     * Stored as a string rather than {@code java.util.Locale} for portability
     * and to avoid serialisation issues across JPA providers.
     */
    @Column(name = "locale", nullable = false, length = 20)
    private String locale;

    /**
     * The translated message text. May contain Spring MessageFormat placeholders
     * ({@code {0}}, {@code {1}}, …) consistent with {@code MessageSource} contracts.
     */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Optional tenant identifier for multi-tenant deployments.
     * {@code null} in single-tenant mode — the column is nullable.
     */
    @Column(name = "tenant_id", nullable = true, length = 100)
    private String tenantId;

    @Embedded
    private AuditMetadata audit = new AuditMetadata();

    // ── Constructors ─────────────────────────────────────────────────────────

    /** JPA requires a no-arg constructor. Not for direct use. */
    protected I18nMessage() {}

    private I18nMessage(String code, String locale, String message,
                        String tenantId, TranslationSource source) {
        requireNonBlank(code,    "code");
        requireNonBlank(locale,  "locale");
        Objects.requireNonNull(message, "message must not be null");
        this.code     = code.trim();
        this.locale   = locale.trim();
        this.message  = message;
        this.tenantId = tenantId;
        this.audit    = AuditMetadata.forSource(source);
    }

    // ── Factory methods ──────────────────────────────────────────────────────

    /** Creates a translation record sourced from the DB (manual entry). */
    public static I18nMessage create(String code, String locale, String message) {
        return new I18nMessage(code, locale, message, null, TranslationSource.DB);
    }

    /** Creates a translation record sourced from the DB for a specific tenant. */
    public static I18nMessage createForTenant(String code, String locale,
                                              String message, String tenantId) {
        requireNonBlank(tenantId, "tenantId");
        return new I18nMessage(code, locale, message, tenantId, TranslationSource.DB);
    }

    /** Creates a translation record marking it as AI-generated. */
    public static I18nMessage createFromAi(String code, String locale,
                                           String message, String tenantId) {
        return new I18nMessage(code, locale, message, tenantId, TranslationSource.AI);
    }

    // ── Behaviour ────────────────────────────────────────────────────────────

    /**
     * Updates the message text and marks the audit source accordingly.
     * Called when an AI-generated translation is later corrected manually.
     */
    public void updateMessage(String newMessage, TranslationSource source) {
        Objects.requireNonNull(newMessage, "newMessage must not be null");
        this.message = newMessage;
        this.audit.setSource(source);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Long           getId()       { return id; }
    public String         getCode()     { return code; }
    public String         getLocale()   { return locale; }
    public String         getMessage()  { return message; }
    public String         getTenantId() { return tenantId; }
    public AuditMetadata  getAudit()    { return audit; }

    public boolean isAiGenerated()      { return audit.isAiGenerated(); }
    public TranslationSource getSource(){ return audit.getSource(); }

    // ── Equality (by business key, not surrogate id) ─────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof I18nMessage that)) return false;
        return Objects.equals(code, that.code)
            && Objects.equals(locale, that.locale)
            && Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, locale, tenantId);
    }

    @Override
    public String toString() {
        return "I18nMessage{code='%s', locale='%s', tenant='%s', source=%s}"
            .formatted(code, locale, tenantId, audit.getSource());
    }

    // ── Guard ────────────────────────────────────────────────────────────────

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
