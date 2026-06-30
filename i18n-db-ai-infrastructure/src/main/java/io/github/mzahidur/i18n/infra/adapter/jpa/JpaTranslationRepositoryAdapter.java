package io.github.mzahidur.i18n.infra.adapter.jpa;

import io.github.mzahidur.i18n.domain.model.I18nMessage;
import io.github.mzahidur.i18n.domain.port.TranslationRepositoryPort;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implements {@link TranslationRepositoryPort} using Spring Data JPA.
 *
 * <p>{@link I18nMessage} is the JPA entity itself (defined in the domain
 * module) — this adapter performs no entity-to-domain mapping, only
 * delegation to {@link I18nMessageJpaRepository}.</p>
 *
 * <p>All methods are tenant-aware per the port contract: {@code null}
 * {@code tenantId} operates in single-tenant mode.</p>
 */
public class JpaTranslationRepositoryAdapter implements TranslationRepositoryPort {

    private final I18nMessageJpaRepository repository;

    public JpaTranslationRepositoryAdapter(I18nMessageJpaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    // ── Reads ─────────────────────────────────────────────────────────────────

    @Override
    public Optional<I18nMessage> findByCodeAndLocale(String code, String locale, String tenantId) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        return repository.findByCodeAndLocaleAndTenant(code, locale, tenantId);
    }

    @Override
    public List<I18nMessage> findAllByLocale(String locale, String tenantId) {
        Objects.requireNonNull(locale, "locale must not be null");
        return repository.findAllByLocaleAndTenant(locale, tenantId);
    }

    @Override
    public List<I18nMessage> findAllByCode(String code, String tenantId) {
        Objects.requireNonNull(code, "code must not be null");
        return repository.findAllByCodeAndTenant(code, tenantId);
    }

    @Override
    public boolean existsByCodeAndLocale(String code, String locale, String tenantId) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        return repository.existsByCodeAndLocaleAndTenant(code, locale, tenantId);
    }

    // ── Writes ────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Upsert semantics: if a record already exists for the same
     * {@code (code, locale, tenantId)}, its message and audit source are
     * updated in place rather than inserting a duplicate row (which would
     * violate the {@code uq_i18n_code_locale} unique constraint).</p>
     */
    @Override
    public I18nMessage save(I18nMessage message) {
        Objects.requireNonNull(message, "message must not be null");

        Optional<I18nMessage> existing = repository.findByCodeAndLocaleAndTenant(
                message.getCode(), message.getLocale(), message.getTenantId());

        if (existing.isPresent()) {
            I18nMessage current = existing.get();
            current.updateMessage(message.getMessage(), message.getSource());
            return repository.save(current);
        }

        return repository.save(message);
    }

    @Override
    public List<I18nMessage> saveAll(Collection<I18nMessage> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        return messages.stream().map(this::save).toList();
    }

    @Override
    public void deleteByCodeAndLocale(String code, String locale, String tenantId) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        repository.deleteByCodeAndLocaleAndTenant(code, locale, tenantId);
    }

    @Override
    public void deleteAllByTenantId(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        repository.deleteAllByTenantId(tenantId);
    }
}
