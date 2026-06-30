package io.github.mzahidur.i18n.infra.adapter.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * JPA entity mapping the {@code i18n_messages} table.
 *
 * <p>Intentionally separate from the domain {@code I18nMessage} model so that
 * JPA annotations never leak into the domain layer.  The repository adapter
 * maps between the two representations.</p>
 *
 * <p>{@link #updatedAt} is managed by Hibernate's {@code @UpdateTimestamp} —
 * no DB trigger is needed.</p>
 */
@Entity
@Table(
        name = "i18n_messages",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_i18n_messages_code_locale",
                columnNames = {"message_code", "locale"}
        )
)
public class I18nMessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_code", nullable = false, length = 255)
    private String code;

    @Column(name = "locale", nullable = false, length = 20)
    private String locale;

    @Column(name = "message_value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private io.github.mzahidur.i18n.domain.model.TranslationSource source;

    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected I18nMessageJpaEntity() {
        // JPA no-arg constructor
    }

    public I18nMessageJpaEntity(String code,
                                 String locale,
                                 String value,
                                 io.github.mzahidur.i18n.domain.model.TranslationSource source) {
        this.code = code;
        this.locale = locale;
        this.value = value;
        this.source = source;
        this.aiGenerated = (source == io.github.mzahidur.i18n.domain.model.TranslationSource.AI);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // -------------------------------------------------------------------------
    // Getters (no setters — mutate via dedicated update methods to keep intent clear)
    // -------------------------------------------------------------------------

    public Long getId()          { return id; }
    public String getCode()      { return code; }
    public String getLocale()    { return locale; }
    public String getValue()     { return value; }

    public io.github.mzahidur.i18n.domain.model.TranslationSource getSource() { return source; }

    public boolean isAiGenerated() { return aiGenerated; }
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getUpdatedAt()  { return updatedAt; }

    /** Update the stored translation value and source (e.g. after AI generates a better result). */
    public void updateValue(String newValue,
                            io.github.mzahidur.i18n.domain.model.TranslationSource newSource) {
        this.value = newValue;
        this.source = newSource;
        this.aiGenerated = (newSource == io.github.mzahidur.i18n.domain.model.TranslationSource.AI);
        // updatedAt handled by @UpdateTimestamp
    }
}
