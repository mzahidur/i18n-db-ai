package io.github.mzahidur.i18n.application.service;

import io.github.mzahidur.i18n.domain.port.CacheKeyResolver;

import java.util.Locale;
import java.util.Objects;

/**
 * Default cache key strategy.
 *
 * <p>Produces keys of the form {@code {code}:{locale}}, e.g.
 * {@code "user.greeting:en_US"}.  This is the strategy used when
 * {@code i18n.db.cache.key-resolver=default} (the library default).</p>
 *
 * <p>The locale is formatted using {@link Locale#toString()} which renders as
 * {@code language_COUNTRY_VARIANT} — consistent, compact, and unambiguous.</p>
 *
 * <p>Tenant isolation is <em>not</em> included in this key; use
 * {@link TenantAwareCacheKeyResolver} when multi-tenancy is required.</p>
 */
public class DefaultCacheKeyResolver implements CacheKeyResolver {

    /** Separator between code and locale segments. */
    private static final char SEPARATOR = ':';

    @Override
    public String resolve(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        return code + SEPARATOR + locale;
    }
}