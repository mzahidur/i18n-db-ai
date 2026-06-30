package io.github.mzahidur.i18n.infra.adapter.cache;

import io.github.mzahidur.i18n.domain.port.CachePort;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Redis implementation of {@link CachePort} backed by {@link StringRedisTemplate}.
 *
 * <p>All keys are prefixed with {@code i18n:} to avoid collision with other
 * Redis key spaces in the host application.  The prefix is prepended
 * transparently in every method — callers pass bare cache keys (as produced
 * by the {@code CacheKeyResolver}) without needing to know about it.</p>
 *
 * <p>Active when {@code i18n.db.cache.type=redis} or when
 * {@code i18n.db.cache.type=auto} and {@code spring-data-redis} +
 * a Lettuce/Jedis driver are on the classpath.</p>
 *
 * <h3>TTL</h3>
 * <p>The {@code ttl} parameter is injected from {@code i18n.db.cache.ttl}
 * (default {@code 3600s}).  A {@code null} or zero duration means keys are
 * stored without expiry — not recommended for production.</p>
 */
public class RedisCacheAdapter implements CachePort {

    /** Namespace prefix for all library keys in Redis. */
    private static final String KEY_PREFIX = "i18n:";

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    /**
     * @param redisTemplate Spring Data Redis template (String serializer on both ends)
     * @param ttl           expiry applied on every {@link #put}; {@code null} = no expiry
     */
    public RedisCacheAdapter(StringRedisTemplate redisTemplate, Duration ttl) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate must not be null");
        this.ttl = ttl; // nullable — handled in put()
    }

    @Override
    public Optional<String> get(String key) {
        Objects.requireNonNull(key, "key must not be null");
        return Optional.ofNullable(redisTemplate.opsForValue().get(prefixed(key)));
    }

    @Override
    public void put(String key, String value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");

        String prefixedKey = prefixed(key);
        if (ttl != null && !ttl.isZero()) {
            redisTemplate.opsForValue().set(prefixedKey, value, ttl);
        } else {
            redisTemplate.opsForValue().set(prefixedKey, value);
        }
    }

    @Override
    public void evict(String key) {
        Objects.requireNonNull(key, "key must not be null");
        redisTemplate.delete(prefixed(key));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@code SCAN} (via {@code keys(pattern)}) to find all Redis keys
     * matching {@code i18n:{codePrefix}:*} then issues a bulk {@code DEL}.
     * Note: {@code keys()} is O(N) on the keyspace — for very large deployments
     * consider partitioning translations into a dedicated Redis instance.</p>
     */
    @Override
    public void evictByCodePrefix(String codePrefix) {
        Objects.requireNonNull(codePrefix, "codePrefix must not be null");
        String pattern = KEY_PREFIX + codePrefix + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public void evictAll() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String prefixed(String key) {
        return KEY_PREFIX + key;
    }
}
