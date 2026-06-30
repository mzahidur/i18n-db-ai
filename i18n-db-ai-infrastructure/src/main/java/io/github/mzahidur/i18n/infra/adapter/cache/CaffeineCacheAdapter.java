package io.github.mzahidur.i18n.infra.adapter.cache;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.mzahidur.i18n.domain.port.CachePort;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * In-process Caffeine implementation of {@link CachePort}.
 *
 * <p>The {@link Cache} instance is constructed and configured (default TTL,
 * max size) in {@code CacheAutoConfig} and injected here.</p>
 *
 * <p>Active when {@code i18n.db.cache.type=caffeine} or when
 * {@code i18n.db.cache.type=auto} and no Redis client is on the classpath.</p>
 *
 * <h3>Per-entry TTL</h3>
 * <p>Caffeine's {@code expireAfterWrite} is a single global policy set at
 * cache-construction time; it has no native per-key TTL override. The
 * {@link #put(String, String, Duration)} overload here therefore stores the
 * value normally (Caffeine policy applies) — true per-locale TTL is only
 * fully honoured by {@link RedisCacheAdapter}.  This is documented as a
 * known Caffeine limitation, not a bug.</p>
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

    /**
     * {@inheritDoc}
     *
     * <p>Caffeine has no per-entry TTL API; the {@code ttl} parameter is
     * accepted for interface compatibility but the entry follows the cache's
     * global {@code expireAfterWrite} policy set at construction time.</p>
     */
    @Override
    public void put(String key, String value, Duration ttl) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        cache.put(key, value);
    }

    @Override
    public void evict(String key) {
        Objects.requireNonNull(key, "key must not be null");
        cache.invalidate(key);
    }

    @Override
    public void evictByPrefix(String keyPrefix) {
        Objects.requireNonNull(keyPrefix, "keyPrefix must not be null");
        cache.asMap().keySet().removeIf(k -> k.startsWith(keyPrefix));
    }

    @Override
    public void evictAll() {
        cache.invalidateAll();
    }

    @Override
    public Set<String> keys() {
        return Set.copyOf(cache.asMap().keySet());
    }

    @Override
    public long size() {
        return cache.estimatedSize();
    }

    @Override
    public String backendName() {
        return "caffeine";
    }
}
