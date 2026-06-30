package io.github.mzahidur.i18n.application.service.cache;

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
 * <p>Tenant isolation is <em>not</em> reflected in the key shape — the
 * {@code tenantId} parameter is accepted (per the {@link CacheKeyResolver}
 * contract) but deliberately ignored, since this resolver is intended for
 * single-tenant deployments. Use {@link TenantAwareCacheKeyResolver} when
 * multi-tenancy is required.</p>
 */
public class DefaultCacheKeyResolver implements CacheKeyResolver {

    /** Separator between key segments. */
    private static final char SEPARATOR = ':';

    @Override
    public String resolve(String code, Locale locale, String tenantId) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        return code + SEPARATOR + locale;
    }

    @Override
    public String codePrefix(String code, String tenantId) {
        Objects.requireNonNull(code, "code must not be null");
        return code + SEPARATOR;
    }

    @Override
    public String localePrefix(Locale locale, String tenantId) {
        Objects.requireNonNull(locale, "locale must not be null");
        // No code segment precedes locale in this key shape, so a locale-only
        // prefix cannot be expressed as a leading substring. Returning an
        // empty prefix is the safest contract-compliant behaviour here —
        // callers relying on locale-prefix eviction should use
        // TenantAwareCacheKeyResolver or a custom resolver instead.
        return "";
    }

    @Override
    public String tenantPrefix(String tenantId) {
        // This resolver does not encode tenant information into the key at
        // all, so there is no meaningful prefix to return. An empty prefix
        // matches every key, which is the correct behaviour for a
        // single-tenant resolver (the entire cache IS the "tenant").
        return "";
    }
}
