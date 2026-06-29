package io.github.mzahidur.i18n.domain.port;

import java.util.Locale;

/**
 * Strategy interface for computing cache keys.
 *
 * <p>Decouples the key structure from the cache and translation chain,
 * allowing host applications to substitute their own key strategy without
 * touching any other component.</p>
 *
 * <p>Built-in implementations (application layer):</p>
 * <ul>
 *   <li>{@code DefaultCacheKeyResolver}     — {@code "{code}:{locale}"}</li>
 *   <li>{@code TenantAwareCacheKeyResolver} — {@code "{tenantId}:{code}:{locale}"}</li>
 * </ul>
 *
 * <p>Host applications provide a custom implementation by declaring a Spring
 * bean of this type — auto-config backs off via {@code @ConditionalOnMissingBean}.</p>
 *
 * <p>Implementations must be stateless and thread-safe.</p>
 */
public interface CacheKeyResolver {

    /**
     * Computes a cache key for the given translation lookup parameters.
     *
     * <p>The returned key must be:</p>
     * <ul>
     *   <li>Deterministic — same inputs always produce the same key.</li>
     *   <li>Unique per logical translation — different codes/locales/tenants
     *       must not collide.</li>
     *   <li>Safe for use as a Redis key or Caffeine map key (no control chars).</li>
     * </ul>
     *
     * @param code      the message key (e.g. {@code "user.welcome.title"})
     * @param locale    the resolved locale for this lookup
     * @param tenantId  the tenant identifier; {@code null} in single-tenant mode
     * @return a non-null, non-blank cache key string
     */
    String resolve(String code, Locale locale, String tenantId);

    /**
     * Computes the prefix used to evict all cache entries for a given code
     * across all locales.
     *
     * <p>The prefix must be a proper prefix of every key produced by
     * {@link #resolve(String, Locale, String)} for the same {@code code}.</p>
     *
     * @param code      the message key
     * @param tenantId  the tenant identifier; {@code null} in single-tenant mode
     * @return prefix string for bulk eviction by code
     */
    String codePrefix(String code, String tenantId);

    /**
     * Computes the prefix used to evict all cache entries for a given locale
     * across all codes.
     *
     * @param locale    the locale
     * @param tenantId  the tenant identifier; {@code null} in single-tenant mode
     * @return prefix string for bulk eviction by locale
     */
    String localePrefix(Locale locale, String tenantId);

    /**
     * Computes the prefix used to evict all cache entries for a given tenant.
     * Returns an empty string in single-tenant mode (effectively matching all keys).
     *
     * @param tenantId the tenant identifier; {@code null} returns {@code ""}
     * @return prefix string for full tenant eviction
     */
    String tenantPrefix(String tenantId);
}
