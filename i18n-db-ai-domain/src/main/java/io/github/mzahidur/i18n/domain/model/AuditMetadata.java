package io.github.mzahidur.i18n.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

/**
 * Reusable audit metadata embedded into JPA entities.
 *
 * <p>Tracks when a record was created and last updated, whether its value
 * was AI-generated, and which layer of the fallback chain produced it.</p>
 *
 * <p>Lifecycle hooks ({@link PrePersist}, {@link PreUpdate}) are defined here
 * rather than in the owning entity so audit behaviour is self-contained and
 * not scattered across subclasses.</p>
 *
 * <p>This class is intentionally framework-light: it uses only JPA annotations
 * (no Hibernate-specific {@code @CreationTimestamp} / {@code @UpdateTimestamp})
 * so it remains portable across JPA providers.</p>
 */
@Embeddable
public class AuditMetadata {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * {@code true} when this translation was produced by an AI provider.
     * Allows operators to audit, review, or purge AI-generated entries.
     */
    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated = false;

    /**
     * The fallback chain layer that produced this translation.
     * Stored as a string for readability in the database.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private TranslationSource source = TranslationSource.DB;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @PrePersist
    void onPrePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    // ── Factory methods ──────────────────────────────────────────────────────

    /**
     * Creates audit metadata for a manually managed (DB or properties) entry.
     */
    public static AuditMetadata forSource(TranslationSource source) {
        AuditMetadata meta = new AuditMetadata();
        meta.source      = source;
        meta.aiGenerated = (source == TranslationSource.AI);
        return meta;
    }

    /**
     * Creates audit metadata explicitly marking an AI-generated entry.
     */
    public static AuditMetadata forAi() {
        return forSource(TranslationSource.AI);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Instant getCreatedAt()        { return createdAt; }
    public Instant getUpdatedAt()        { return updatedAt; }
    public boolean isAiGenerated()       { return aiGenerated; }
    public TranslationSource getSource() { return source; }

    // ── Package-private setters (infrastructure layer use only) ──────────────

    void setSource(TranslationSource source) {
        this.source      = source;
        this.aiGenerated = (source == TranslationSource.AI);
    }
}
