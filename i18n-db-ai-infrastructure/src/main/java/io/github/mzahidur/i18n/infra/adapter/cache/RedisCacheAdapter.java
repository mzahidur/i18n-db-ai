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
 * Redis key spaces in the host application. The prefix is transparent to
 * callers — bare keys (as produced by {@code CacheKeyResolver}) go in, bare
 * keys come back out via {@link #keys()}.</p>
 *
 * <p>Active when {@code i18n.db.cache.type=redis} or when
 * {@code i18n.db.cache.type=auto} and {@code spring-data-redis} +
 * a Lettuce/Jedis driver are on the classpath.</p>
 *
 * <h3>TTL</h3>
 * <p>{@link #put(String, String)} applies the configured default TTL.
 * {@link #put(String, String, Duration)} applies a per-call override — this
 * is where Redis genuinely supports per-locale TTL, unlike Caffeine.</p>
 */
public class RedisCacheAdapter implements CachePort {

    private static final String KEY_PREFIX = "i18n:";

    private final StringRedisTemplate redisTemplate;
    private final Duration defaultTtl;

    /**
     * @param redisTemplate Spring Data Redis template (String serializer on both ends)
     * @param defaultTtl    default expiry applied by {@link #put(String, String)};
     *                      {@code null} or zero = no expiry
     */
    public RedisCacheAdapter(StringRedisTemplate redisTemplate, Duration defaultTtl) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate must not be null");
        this.defaultTtl = defaultTtl;
    }

    @Override
    public Optional<String> get(String key) {
        Objects.requireNonNull(key, "key must not be null");
        return Optional.ofNullable(redisTemplate.opsForValue().get(prefixed(key)));
    }

    @Override
    public void put(String key, String value) {
        put(key, value, defaultTtl);
    }

    @Override
    public void put(String key, String value, Duration ttl) {
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
     * <p>Uses {@code KEYS} (via {@code keys(pattern)}) to find matching keys then
     * issues a bulk {@code DEL}. O(N) on keyspace size — acceptable for
     * translation-cache scale; for very large deployments consider a
     * dedicated Redis instance.</p>
     */
    @Override
    public void evictByPrefix(String keyPrefix) {
        Objects.requireNonNull(keyPrefix, "keyPrefix must not be null");
        Set<String> matched = redisTemplate.keys(KEY_PREFIX + keyPrefix + "*");
        if (matched != null && !matched.isEmpty()) {
            redisTemplate.delete(matched);
        }
    }

    @Override
    public void evictAll() {
        Set<String> matched = redisTemplate.keys(KEY_PREFIX + "*");
        if (matched != null && !matched.isEmpty()) {
            redisTemplate.delete(matched);
        }
    }

    @Override
    public Set<String> keys() {
        Set<String> raw = redisTemplate.keys(KEY_PREFIX + "*");
        if (raw == null || raw.isEmpty()) {
            return Set.of();
        }
        return raw.stream()
                .map(k -> k.substring(KEY_PREFIX.length()))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public long size() {
        Set<String> matched = redisTemplate.keys(KEY_PREFIX + "*");
        return matched == null ? 0L : matched.size();
    }

    @Override
    public String backendName() {
        return "redis";
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String prefixed(String key) {
        return KEY_PREFIX + key;
    }
}
