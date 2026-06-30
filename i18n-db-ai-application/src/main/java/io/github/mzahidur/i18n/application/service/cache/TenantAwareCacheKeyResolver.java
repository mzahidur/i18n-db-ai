package io.github.mzahidur.i18n.application.service.cache;

import io.github.mzahidur.i18n.domain.port.CacheKeyResolver;
import io.github.mzahidur.i18n.domain.port.TenantIdResolver;

import java.util.Locale;
import java.util.Objects;

/**
 * Tenant-aware cache key strategy.
 *
 * <p>Produces keys of the form {@code {tenantId}:{code}:{locale}}, e.g.
 * {@code "acme-corp:user.greeting:en_US"}.  This ensures that translations
 * belonging to different tenants do not share cache entries even when they
 * use the same message code and locale.</p>
 *
 * <p>This strategy is activated when
 * {@code i18n.db.cache.key-resolver=tenant-aware}.  It requires
 * {@code i18n.db.tenant.enabled=true} and a {@link TenantIdResolver} bean
 * to be present in the application context.</p>
 *
 * <p>If the resolved tenant ID is blank or null a fallback of {@code "_global_"}
 * is used, mirroring the behaviour of the default (single-tenant) key.</p>
 */
public class TenantAwareCacheKeyResolver implements CacheKeyResolver {

    private static final char SEPARATOR = ':';
    private static final String FALLBACK_TENANT = "_global_";

    private final TenantIdResolver tenantIdResolver;

    public TenantAwareCacheKeyResolver(TenantIdResolver tenantIdResolver) {
        this.tenantIdResolver = Objects.requireNonNull(tenantIdResolver, "tenantIdResolver must not be null");
    }

    @Override
    public String resolve(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        String tenantId = tenantIdResolver.currentTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = FALLBACK_TENANT;
        }

        return tenantId + SEPARATOR + code + SEPARATOR + locale;
    }
}