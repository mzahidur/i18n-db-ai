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
 *
 * <p>Because the explicit {@code tenantId} parameter present on every
 * {@link CacheKeyResolver} method takes precedence when supplied, the
 * injected {@link TenantIdResolver} is only consulted as a fallback when the
 * caller passes {@code null} — this lets callers override the ambient
 * tenant context explicitly (e.g. admin tooling acting on behalf of a
 * specific tenant) while still defaulting to request-scoped tenant
 * resolution in the common case.</p>
 */
public class TenantAwareCacheKeyResolver implements CacheKeyResolver {

    private static final char SEPARATOR = ':';
    private static final String FALLBACK_TENANT = "_global_";

    private final TenantIdResolver tenantIdResolver;

    public TenantAwareCacheKeyResolver(TenantIdResolver tenantIdResolver) {
        this.tenantIdResolver = Objects.requireNonNull(tenantIdResolver, "tenantIdResolver must not be null");
    }

    @Override
    public String resolve(String code, Locale locale, String tenantId) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        String effectiveTenant = effectiveTenantId(tenantId);
        return effectiveTenant + SEPARATOR + code + SEPARATOR + locale;
    }

    @Override
    public String codePrefix(String code, String tenantId) {
        Objects.requireNonNull(code, "code must not be null");

        String effectiveTenant = effectiveTenantId(tenantId);
        return effectiveTenant + SEPARATOR + code + SEPARATOR;
    }

    @Override
    public String localePrefix(Locale locale, String tenantId) {
        Objects.requireNonNull(locale, "locale must not be null");

        // No code segment precedes locale in this key shape, so a
        // locale-only prefix within a tenant cannot be expressed as a
        // leading substring (tenant:CODE:locale — the code sits between
        // tenant and locale). Returning the tenant-only prefix is the
        // most useful contract-compliant approximation: it narrows
        // eviction to the tenant, even though it cannot narrow further
        // to a single locale across all codes.
        return effectiveTenantId(tenantId) + SEPARATOR;
    }

    @Override
    public String tenantPrefix(String tenantId) {
        return effectiveTenantId(tenantId) + SEPARATOR;
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private String effectiveTenantId(String explicitTenantId) {
        if (explicitTenantId != null && !explicitTenantId.isBlank()) {
            return explicitTenantId;
        }

        String resolved = tenantIdResolver.currentTenantId();
        if (resolved == null || resolved.isBlank()) {
            return FALLBACK_TENANT;
        }
        return resolved;
    }
}
