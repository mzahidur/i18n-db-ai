package io.github.mzahidur.i18n.domain.port;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Secondary port for AI-powered translation generation.
 *
 * <p>Abstracts the AI provider entirely from the domain and application layers.
 * Concrete implementations live in the infrastructure layer:</p>
 * <ul>
 *   <li>{@code NoOpAiTranslationService}      — Null Object default (no AI configured)</li>
 *   <li>{@code SpringAiTranslationService}    — Spring AI / OpenAI implementation</li>
 *   <li>{@code LangChain4jTranslationService} — LangChain4j implementation</li>
 * </ul>
 *
 * <p>Implementations must be stateless and thread-safe.</p>
 */
public interface AiTranslationPort {

    /**
     * Translates a source text into the target locale.
     *
     * <p>The {@code context} map allows callers to pass optional hints to the AI
     * provider — e.g. domain ({@code "ui_label"}), tone ({@code "formal"}),
     * or surrounding text for disambiguation. Keys and values are provider-specific;
     * implementations should ignore unknown keys gracefully.</p>
     *
     * @param sourceText   the original text to translate (in {@code sourceLocale})
     * @param sourceLocale the locale of the source text
     * @param targetLocale the desired output locale
     * @param context      optional provider hints; may be empty, never null
     * @return the translated text, or {@link Optional#empty()} if the provider
     *         cannot translate (e.g. NoOp, unsupported locale, rate-limited)
     */
    Optional<String> translate(String sourceText,
                               Locale sourceLocale,
                               Locale targetLocale,
                               Map<String, String> context);

    /**
     * Convenience overload — translates with no additional context hints.
     *
     * @param sourceText   the original text
     * @param sourceLocale the locale of the source text
     * @param targetLocale the desired output locale
     * @return the translated text, or {@link Optional#empty()}
     */
    default Optional<String> translate(String sourceText,
                                       Locale sourceLocale,
                                       Locale targetLocale) {
        return translate(sourceText, sourceLocale, targetLocale, Map.of());
    }

    /**
     * Returns {@code true} if this implementation can produce translations
     * (i.e. is not the NoOp stub and is properly configured).
     *
     * <p>Application layer callers use this to decide whether to attempt
     * AI translation or skip the fallback step entirely.</p>
     */
    boolean isAvailable();

    /**
     * A human-readable identifier for this AI provider.
     * Used in logs and audit records (e.g. {@code "spring-ai"}, {@code "langchain4j"}, {@code "noop"}).
     */
    String providerName();
}
