package io.github.mzahidur.i18n.infra.adapter.jpa;

import io.github.mzahidur.i18n.domain.model.TranslationSource;
import io.github.mzahidur.i18n.domain.port.TranslationRepositoryPort;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Implements {@link TranslationRepositoryPort} using Spring Data JPA.
 *
 * <p>Locale resolution strategy (mirrors Spring's own message source behaviour):</p>
 * <ol>
 *   <li>Exact match: {@code code} + {@code locale.toString()} (e.g. {@code "en_US"})</li>
 *   <li>Language-only fallback: {@code code} + {@code locale.getLanguage()} (e.g. {@code "en"})</li>
 * </ol>
 *
 * <p>Persistence: {@link #save} performs an upsert — if a row already exists for
 * the (code, locale) pair its value is updated in-place; otherwise a new row is
 * inserted.  This keeps AI-generated translations idempotent across restarts.</p>
 */
public class JpaTranslationRepositoryAdapter implements TranslationRepositoryPort {

    private final I18nMessageJpaRepository repository;

    public JpaTranslationRepositoryAdapter(I18nMessageJpaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> findTranslation(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        // 1. Exact locale match (e.g. "en_US")
        Optional<I18nMessageJpaEntity> exact =
                repository.findByCodeAndLocale(code, locale.toString());
        if (exact.isPresent()) {
            return Optional.of(exact.get().getValue());
        }

        // 2. Language-only fallback (e.g. "en") — only if locale had country/variant
        if (!locale.getCountry().isEmpty() || !locale.getVariant().isEmpty()) {
            return repository
                    .findByCodeAndLanguage(code, locale.getLanguage())
                    .map(I18nMessageJpaEntity::getValue);
        }

        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public void save(String code, Locale locale, String value, TranslationSource source) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(source, "source must not be null");

        String localeStr = locale.toString();

        repository.findByCodeAndLocale(code, localeStr)
                .ifPresentOrElse(
                        existing -> existing.updateValue(value, source),
                        () -> repository.save(new I18nMessageJpaEntity(code, localeStr, value, source))
                );
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        return repository.existsByCodeAndLocale(code, locale.toString());
    }

    /** {@inheritDoc} */
    @Override
    public void deleteByCode(String code) {
        Objects.requireNonNull(code, "code must not be null");
        repository.deleteByCode(code);
    }
}
