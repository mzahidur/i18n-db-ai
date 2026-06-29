package io.github.mzahidur.i18n.application.service;

import io.github.mzahidur.i18n.domain.port.CacheKeyResolver;
import io.github.mzahidur.i18n.domain.port.CachePort;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Top-level use case orchestrator for translation resolution.
 *
 * <p>This is the single entry point consumed by the infrastructure adapter
 * {@code DatabaseMessageSource}.  It composes the cache and the fallback chain
 * following the Decorator pattern described in the build plan:</p>
 *
 * <pre>
 *  CachePort (read)
 *      └─ hit  → return cached value
 *      └─ miss → TranslationChain.resolve(code, locale)
 *                    └─ DB → Properties → AI
 *                └─ CachePort (write) → return resolved value
 * </pre>
 *
 * <h3>Thread safety</h3>
 * <p>All collaborators ({@link CachePort}, {@link TranslationChain},
 * {@link CacheKeyResolver}) are required to be thread-safe.  This service
 * itself holds no mutable state and is safe for concurrent use.</p>
 *
 * <h3>Dependency rule</h3>
 * <p>This class MUST NOT import any Spring, JPA, or infrastructure class.
 * It depends only on domain ports and application-layer siblings.</p>
 */
public class TranslationApplicationService {

    private final TranslationChain chain;
    private final CachePort cache;
    private final CacheKeyResolver keyResolver;

    /**
     * @param chain       the ordered fallback chain
     * @param cache       the cache backend (Caffeine, Redis, or no-op)
     * @param keyResolver the cache key strategy in use
     */
    public TranslationApplicationService(TranslationChain chain,
                                         CachePort cache,
                                         CacheKeyResolver keyResolver) {
        this.chain = Objects.requireNonNull(chain, "chain must not be null");
        this.cache = Objects.requireNonNull(cache, "cache must not be null");
        this.keyResolver = Objects.requireNonNull(keyResolver, "keyResolver must not be null");
    }

    /**
     * Resolve a translation, consulting the cache before the fallback chain.
     *
     * <p>On a cache miss the full chain is walked (DB → Properties → AI).
     * A successful result is written back to the cache before returning.</p>
     *
     * @param code   message code
     * @param locale target locale
     * @return the resolved translation string
     * @throws io.github.mzahidur.i18n.domain.exception.TranslationNotFoundException
     *         if no provider in the chain could supply a translation
     */
    public String getMessage(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        String cacheKey = keyResolver.resolve(code, locale);

        // 1. Cache read
        Optional<String> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 2. Fallback chain walk (DB → Properties → AI)
        String resolved = chain.resolve(code, locale);

        // 3. Cache write
        cache.put(cacheKey, resolved);

        return resolved;
    }

    /**
     * Check whether a translation exists without throwing on a miss.
     *
     * <p>Useful for conditional rendering in view templates.  Internally walks
     * the full resolution path — the result is cached as a side-effect.</p>
     *
     * @param code   message code
     * @param locale target locale
     * @return {@code true} if a translation can be resolved; {@code false} otherwise
     */
    public boolean hasMessage(String code, Locale locale) {
        try {
            getMessage(code, locale);
            return true;
        } catch (io.github.mzahidur.i18n.domain.exception.TranslationNotFoundException ex) {
            return false;
        }
    }
}