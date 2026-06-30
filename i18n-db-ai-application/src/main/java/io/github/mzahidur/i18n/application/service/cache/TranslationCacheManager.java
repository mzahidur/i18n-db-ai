package io.github.mzahidur.i18n.application.service.cache;

import io.github.mzahidur.i18n.domain.port.CacheKeyResolver;
import io.github.mzahidur.i18n.domain.port.CachePort;

import java.util.Locale;
import java.util.Objects;

/**
 * Application-layer façade for cache eviction operations.
 *
 * <p>Exposes a clean, use-case-oriented API for invalidating translation cache
 * entries.  All key construction is delegated to the configured
 * {@link CacheKeyResolver}, ensuring consistency with the read path in
 * {@link io.github.mzahidur.i18n.application.service.TranslationApplicationService}.</p>
 *
 * <h3>Use cases</h3>
 * <ul>
 *   <li>{@link #evict(String, Locale, String)} — invalidate a single code/locale pair after an admin update</li>
 *   <li>{@link #evictByCode(String, String)}    — invalidate all locales for a given code</li>
 *   <li>{@link #evictByTenant(String)}          — invalidate every cached entry for a tenant</li>
 *   <li>{@link #evictAll()}                     — full cache flush (e.g. after a bulk import)</li>
 * </ul>
 *
 * <p>This class is exposed as a Spring bean in the starter so that host
 * applications can inject it into their admin controllers or scheduled jobs.</p>
 */
public class TranslationCacheManager {

    private final CachePort cache;
    private final CacheKeyResolver keyResolver;

    public TranslationCacheManager(CachePort cache, CacheKeyResolver keyResolver) {
        this.cache = Objects.requireNonNull(cache, "cache must not be null");
        this.keyResolver = Objects.requireNonNull(keyResolver, "keyResolver must not be null");
    }

    /**
     * Evict the cache entry for a specific code/locale/tenant combination.
     *
     * @param code     message code to evict
     * @param locale   locale to evict
     * @param tenantId tenant identifier; {@code null} for single-tenant
     */
    public void evict(String code, Locale locale, String tenantId) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        String key = keyResolver.resolve(code, locale, tenantId);
        cache.evict(key);
    }

    /**
     * Convenience overload for single-tenant deployments.
     *
     * @param code   message code to evict
     * @param locale locale to evict
     */
    public void evict(String code, Locale locale) {
        evict(code, locale, null);
    }

    /**
     * Evict all cached locales for a given message code.
     *
     * <p>Delegates to {@link CachePort#evictByPrefix(String)} using the prefix
     * produced by {@link CacheKeyResolver#codePrefix(String, String)}. This
     * operation is best-effort: Caffeine supports it via prefix iteration;
     * Redis uses a {@code SCAN} + {@code DEL} pattern. Both implementations
     * are handled in the infrastructure layer.</p>
     *
     * @param code     message code whose locale variants should be purged
     * @param tenantId tenant identifier; {@code null} for single-tenant
     */
    public void evictByCode(String code, String tenantId) {
        Objects.requireNonNull(code, "code must not be null");
        cache.evictByPrefix(keyResolver.codePrefix(code, tenantId));
    }

    /**
     * Convenience overload for single-tenant deployments.
     *
     * @param code message code whose locale variants should be purged
     */
    public void evictByCode(String code) {
        evictByCode(code, null);
    }

    /**
     * Evict every cached entry belonging to a given locale, regardless of code.
     *
     * @param locale   locale whose cached entries should be purged
     * @param tenantId tenant identifier; {@code null} for single-tenant
     */
    public void evictByLocale(Locale locale, String tenantId) {
        Objects.requireNonNull(locale, "locale must not be null");
        cache.evictByPrefix(keyResolver.localePrefix(locale, tenantId));
    }

    /**
     * Evict every cached entry belonging to a given tenant.
     *
     * <p>No-op in single-tenant mode (the resolver returns an empty prefix,
     * which intentionally matches everything via {@link #evictAll()}-equivalent
     * semantics only when the active resolver is tenant-aware; single-tenant
     * resolvers return {@code ""} deliberately to avoid surprising mass
     * eviction from this method).</p>
     *
     * @param tenantId tenant whose cached entries should be purged
     */
    public void evictByTenant(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        cache.evictByPrefix(keyResolver.tenantPrefix(tenantId));
    }

    /**
     * Flush the entire translation cache.
     *
     * <p>Intended for bulk import or deployment scenarios where all cached
     * translations may be stale.</p>
     */
    public void evictAll() {
        cache.evictAll();
    }
}
