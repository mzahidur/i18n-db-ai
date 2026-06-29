package io.github.mzahidur.i18n.domain.port;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Secondary port for caching resolved translations.
 *
 * <p>Deliberately minimal: the cache stores and retrieves strings (resolved
 * message text) keyed by an opaque string produced by {@link CacheKeyResolver}.
 * The domain has no knowledge of Redis, Caffeine, or any cache technology.</p>
 *
 * <p>Concrete adapters live in the infrastructure layer:</p>
 * <ul>
 *   <li>{@code CaffeineCacheAdapter} — in-memory (default when Redis absent)</li>
 *   <li>{@code RedisCacheAdapter}    — distributed (auto-selected when Redis present)</li>
 * </ul>
 *
 * <p>All operations must be non-blocking from the caller's perspective.
 * Implementations should swallow internal errors and degrade gracefully
 * (cache miss) rather than propagating exceptions to the translation chain.</p>
 */
public interface CachePort {

    /**
     * Retrieves a cached translation.
     *
     * @param key the cache key (produced by {@link CacheKeyResolver})
     * @return the cached message text, or {@link Optional#empty()} on a miss
     */
    Optional<String> get(String key);

    /**
     * Stores a translation in the cache with the configured global TTL.
     *
     * @param key   the cache key
     * @param value the resolved message text to cache
     */
    void put(String key, String value);

    /**
     * Stores a translation with an explicit TTL override.
     * Useful for per-locale TTL configuration.
     *
     * @param key   the cache key
     * @param value the resolved message text
     * @param ttl   how long to keep the entry; must be positive
     */
    void put(String key, String value, Duration ttl);

    /**
     * Evicts a single cache entry.
     * No-op if the key does not exist.
     *
     * @param key the cache key to evict
     */
    void evict(String key);

    /**
     * Evicts all cache entries whose keys start with the given prefix.
     *
     * <p>Used by {@code TranslationCacheManager} to flush all entries for
     * a specific code or locale without iterating the full cache.</p>
     *
     * @param keyPrefix the prefix to match (e.g. {@code "en:"}, {@code "user.title:"})
     */
    void evictByPrefix(String keyPrefix);

    /**
     * Evicts all entries in the cache.
     * Use with caution — clears the entire translation cache.
     */
    void evictAll();

    /**
     * Returns all keys currently in the cache.
     *
     * <p>Intended for management/admin endpoints, not the hot translation path.
     * Implementations backed by distributed caches (Redis) may return only
     * a representative sample for very large key sets.</p>
     *
     * @return a snapshot of current cache keys; may be empty, never null
     */
    Set<String> keys();

    /**
     * Returns the approximate number of entries currently in the cache.
     */
    long size();

    /**
     * A human-readable identifier for this cache backend.
     * Used in logs and health endpoints (e.g. {@code "caffeine"}, {@code "redis"}, {@code "noop"}).
     */
    String backendName();
}
