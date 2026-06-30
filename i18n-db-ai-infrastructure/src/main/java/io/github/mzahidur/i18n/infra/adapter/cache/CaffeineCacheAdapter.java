package io.github.mzahidur.i18n.infra.adapter.cache;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.mzahidur.i18n.domain.port.CachePort;

import java.util.Objects;
import java.util.Optional;

/**
 * In-process Caffeine implementation of {@link CachePort}.
 *
 * <p>The {@link Cache} instance is constructed and configured (TTL, max size)
 * in {@code CacheAutoConfig} and injected here.  This adapter is purely a
 * delegation wrapper — all eviction and expiry policy lives in the
 * auto-configuration, not here.</p>
 *
 * <p>Active when {@code i18n.db.cache.type=caffeine} or when
 * {@code i18n.db.cache.type=auto} and no Redis client is on the classpath.</p>
 */
public class CaffeineCacheAdapter implements CachePort {

    private final Cache<String, String> cache;

    public CaffeineCacheAdapter(Cache<String, String> cache) {
        this.cache = Objects.requireNonNull(cache, "cache must not be null");
    }

    @Override
    public Optional<String> get(String key) {
        Objects.requireNonNull(key, "key must not be null");
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    @Override
    public void put(String key, String value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        cache.put(key, value);
    }

    @Override
    public void evict(String key) {
        Objects.requireNonNull(key, "key must not be null");
        cache.invalidate(key);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Caffeine does not support native prefix-based eviction.  This
     * implementation iterates the live entry set and invalidates all keys
     * that start with {@code codePrefix + ":"}.  The iteration is not
     * atomic but is safe for concurrent use — Caffeine's internal map
     * tolerates concurrent modification during {@code asMap()} iteration.</p>
     */
    @Override
    public void evictByCodePrefix(String codePrefix) {
        Objects.requireNonNull(codePrefix, "codePrefix must not be null");
        String prefix = codePrefix + ":";
        cache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
    }

    @Override
    public void evictAll() {
        cache.invalidateAll();
    }
}
