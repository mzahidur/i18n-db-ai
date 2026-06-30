package io.github.mzahidur.i18n.starter.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.mzahidur.i18n.domain.port.CachePort;
import io.github.mzahidur.i18n.infra.adapter.cache.CaffeineCacheAdapter;
import io.github.mzahidur.i18n.infra.adapter.cache.RedisCacheAdapter;
import io.github.mzahidur.i18n.starter.properties.I18nDbProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Auto-configuration for the {@link CachePort} binding.
 *
 * <p>Selection priority when {@code i18n.db.cache.type=auto}:</p>
 * <ol>
 *   <li>Caffeine — if {@code com.github.benmanes.caffeine.cache.Cache} is on the classpath</li>
 *   <li>Redis    — if {@code StringRedisTemplate} is on the classpath</li>
 *   <li>No-op    — fallback; caching disabled, chain is hit on every request</li>
 * </ol>
 *
 * <p>The host application can force a specific backend via
 * {@code i18n.db.cache.type=caffeine|redis|none}.</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "i18n.db", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheAutoConfig {

    // -------------------------------------------------------------------------
    // Caffeine
    // -------------------------------------------------------------------------

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Cache.class)
    @ConditionalOnProperty(prefix = "i18n.db.cache", name = "type",
            havingValue = "caffeine", matchIfMissing = true)
    static class CaffeineConfig {

        @Bean
        @ConditionalOnMissingBean(CachePort.class)
        public CachePort caffeineCachePort(I18nDbProperties properties) {
            Duration ttl = properties.getCache().getTtl();
            Cache<String, String> caffeineCache = Caffeine.newBuilder()
                    .expireAfterWrite(ttl)
                    .maximumSize(10_000)
                    .build();
            return new CaffeineCacheAdapter(caffeineCache);
        }
    }

    // -------------------------------------------------------------------------
    // Redis
    // -------------------------------------------------------------------------

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnProperty(prefix = "i18n.db.cache", name = "type",
            havingValue = "redis")
    static class RedisConfig {

        @Bean
        @ConditionalOnMissingBean(CachePort.class)
        public CachePort redisCachePort(StringRedisTemplate redisTemplate,
                                        I18nDbProperties properties) {
            Duration ttl = properties.getCache().getTtl();
            return new RedisCacheAdapter(redisTemplate, ttl);
        }
    }

    // -------------------------------------------------------------------------
    // No-op fallback (default when neither Caffeine nor Redis is on the
    // classpath, or when i18n.db.cache.type=none)
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(CachePort.class)
    public CachePort noOpCachePort() {
        return new NoOpCacheAdapter();
    }

    /**
     * Inner no-op implementation of the full {@link CachePort} contract.
     * Kept as a private nested class rather than a separate infra file since
     * it has no state, no dependencies, and exists purely as the safe
     * fallback when no real cache backend is configured.
     */
    static class NoOpCacheAdapter implements CachePort {
        @Override public Optional<String> get(String key)                  { return Optional.empty(); }
        @Override public void put(String key, String value)                { /* no-op */ }
        @Override public void put(String key, String value, Duration ttl)  { /* no-op */ }
        @Override public void evict(String key)                            { /* no-op */ }
        @Override public void evictByPrefix(String keyPrefix)              { /* no-op */ }
        @Override public void evictAll()                                   { /* no-op */ }
        @Override public Set<String> keys()                                { return Set.of(); }
        @Override public long size()                                       { return 0L; }
        @Override public String backendName()                              { return "noop"; }
    }
}