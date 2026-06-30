package io.github.mzahidur.i18n.application.service.provider;

import io.github.mzahidur.i18n.domain.port.TranslationRepositoryPort;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * First provider in the fallback chain.
 *
 * <p>Delegates to {@link TranslationRepositoryPort} for a fast DB lookup.
 * Returns {@link Optional#empty()} when the row does not exist, leaving the
 * chain to continue to the next provider.</p>
 */
public class DbTranslationProvider implements TranslationProvider {

    private static final int ORDER = 10;

    private final TranslationRepositoryPort repository;

    public DbTranslationProvider(TranslationRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Queries {@code i18n_messages} for an exact ({@code code}, {@code locale}) match.
     * If the locale has a country/variant, the lookup falls back automatically to the
     * language-only root locale via the repository contract.</p>
     */
    @Override
    public Optional<String> resolve(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        return repository.findTranslation(code, locale);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public String getName() {
        return "DB";
    }
}