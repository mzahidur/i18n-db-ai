package io.github.mzahidur.i18n.infra.adapter.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.mzahidur.i18n.domain.port.AiTranslationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * LangChain4j implementation of {@link AiTranslationPort}.
 *
 * <p>Active when {@code i18n.db.ai.provider=langchain4j} and the
 * {@code langchain4j-open-ai-spring-boot-starter} (or any other LangChain4j
 * model starter) is on the classpath. The {@link ChatLanguageModel} bean is
 * supplied by LangChain4j's own auto-configuration and injected by
 * {@code AiAutoConfig}.</p>
 *
 * <h3>Prompt design</h3>
 * <p>Built as a plain string rather than LangChain4j's {@code PromptTemplate}
 * to keep optional context-hint interpolation simple — hints are appended
 * conditionally and a fixed template doesn't accommodate that cleanly.</p>
 *
 * <h3>Error handling</h3>
 * <p>Any exception is caught and logged; the method returns
 * {@link Optional#empty()} so the fallback chain degrades gracefully.</p>
 */
public class LangChain4jTranslationService implements AiTranslationPort {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jTranslationService.class);

    private static final String BASE_PROMPT =
            "Translate the following text from %s (%s) into %s (%s).\n" +
            "Respond with only the translated text — no explanation, no quotes, no punctuation changes.\n\n" +
            "Text: \"%s\"";

    private final ChatLanguageModel model;

    public LangChain4jTranslationService(ChatLanguageModel model) {
        this.model = Objects.requireNonNull(model, "model must not be null");
    }

    @Override
    public Optional<String> translate(String sourceText,
                                       Locale sourceLocale,
                                       Locale targetLocale,
                                       Map<String, String> context) {
        Objects.requireNonNull(sourceText, "sourceText must not be null");
        Objects.requireNonNull(sourceLocale, "sourceLocale must not be null");
        Objects.requireNonNull(targetLocale, "targetLocale must not be null");
        Objects.requireNonNull(context, "context must not be null (use Map.of() for none)");

        String prompt = buildPrompt(sourceText, sourceLocale, targetLocale, context);

        try {
            String response = model.chat(prompt);

            if (response == null || response.isBlank()) {
                log.warn("LangChain4j returned blank response for sourceLocale='{}' targetLocale='{}'",
                         sourceLocale, targetLocale);
                return Optional.empty();
            }

            String translation = response.strip();
            log.debug("LangChain4j translated '{}' ({} → {}) → '{}'",
                      sourceText, sourceLocale, targetLocale, translation);
            return Optional.of(translation);

        } catch (Exception ex) {
            log.error("LangChain4j translation failed (sourceLocale='{}' targetLocale='{}'): {}",
                      sourceLocale, targetLocale, ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "langchain4j";
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String buildPrompt(String sourceText, Locale sourceLocale, Locale targetLocale,
                               Map<String, String> context) {
        String prompt = String.format(
                BASE_PROMPT,
                sourceLocale.getDisplayLanguage(Locale.ENGLISH), sourceLocale,
                targetLocale.getDisplayLanguage(Locale.ENGLISH), targetLocale,
                sourceText
        );

        if (!context.isEmpty()) {
            StringBuilder hints = new StringBuilder("\n\nAdditional context:");
            context.forEach((k, v) -> hints.append("\n- ").append(k).append(": ").append(v));
            prompt += hints;
        }

        return prompt;
    }
}
