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
 * {@link TranslationApplicationService}.</p>
 *
 * <h3>Use cases</h3>
 * <ul>
 *   <li>{@link #evict(String, Locale)}   — invalidate a single code/locale pair after an admin update</li>
 *   <li>{@link #evictByCode(String)}     — invalidate all locales for a given code (not supported by all cache backends)</li>
 *   <li>{@link #evictAll()}              — full cache flush (e.g. after a bulk import)</li>
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
     * Evict the cache entry for a specific code/locale combination.
     *
     * @param code   message code to evict
     * @param locale locale to evict
     */
    public void evict(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        String key = keyResolver.resolve(code, locale);
        cache.evict(key);
    }

    /**
     * Evict all cached locales for a given message code.
     *
     * <p>This operation is best-effort: Caffeine supports it via prefix
     * iteration; Redis uses a {@code SCAN} + {@code DEL} pattern.  Both
     * implementations are handled in the infrastructure layer.</p>
     *
     * @param code message code whose locale variants should be purged
     */
    public void evictByCode(String code) {
        Objects.requireNonNull(code, "code must not be null");
        cache.evictByCodePrefix(code);
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