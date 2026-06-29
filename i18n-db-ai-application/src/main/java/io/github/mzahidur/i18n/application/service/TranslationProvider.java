package io.github.mzahidur.i18n.application.service;

import java.util.Locale;
import java.util.Optional;

/**
 * Contract for a single step in the translation fallback chain.
 *
 * <p>Implementations are ordered (via {@link #getOrder()}) and tried in
 * ascending order by {@link TranslationChain}.  The first non-empty result
 * short-circuits the chain.</p>
 *
 * <p>Dependency rule: this interface lives in the <em>application</em> layer
 * and may only depend on the <em>domain</em> layer — never on Spring, JPA, or
 * any infrastructure class.</p>
 */
public interface TranslationProvider {

    /**
     * Attempt to resolve a translation for the given message code and locale.
     *
     * @param code   the message code (e.g. {@code "user.greeting"})
     * @param locale the requested locale
     * @return an {@link Optional} containing the translated string, or
     *         {@link Optional#empty()} if this provider cannot fulfil the request
     */
    Optional<String> resolve(String code, Locale locale);

    /**
     * Relative ordering within the fallback chain.
     *
     * <p>Lower values are tried first.  Suggested defaults:</p>
     * <ul>
     *   <li>DB provider        → {@code 10}</li>
     *   <li>Properties provider → {@code 20}</li>
     *   <li>AI provider        → {@code 30}</li>
     * </ul>
     *
     * @return the ordinal position of this provider
     */
    int getOrder();

    /**
     * Human-readable name used in logging and diagnostics.
     *
     * @return provider name (e.g. {@code "DB"}, {@code "PROPERTIES"}, {@code "AI"})
     */
    String getName();
}