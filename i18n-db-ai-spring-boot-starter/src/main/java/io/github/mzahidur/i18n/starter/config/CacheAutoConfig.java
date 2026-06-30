package io.github.mzahidur.i18n.starter.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.mzahidur.i18n.domain.port.CacheKeyResolver;
import io.github.mzahidur.i18n.domain.port.CachePort;
import io.github.mzahidur.i18n.infra.cache.CaffeineCacheAdapter;
import io.github.mzahidur.i18n.infra.cache.RedisCacheAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

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
    // No-op fallback
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(CachePort.class)
    public CachePort noOpCachePort() {
        return new NoOpCacheAdapter();
    }

    // -------------------------------------------------------------------------
    // No-op inner implementation (avoids a separate file for a trivial class)
    // -------------------------------------------------------------------------

    static class NoOpCacheAdapter implements CachePort {
        @Override public java.util.Optional<String> get(String key)          { return java.util.Optional.empty(); }
        @Override public void put(String key, String value)                  { /* no-op */ }
        @Override public void evict(String key)                              { /* no-op */ }
        @Override public void evictByCodePrefix(String codePrefix)           { /* no-op */ }
        @Override public void evictAll()                                     { /* no-op */ }
    }
}
