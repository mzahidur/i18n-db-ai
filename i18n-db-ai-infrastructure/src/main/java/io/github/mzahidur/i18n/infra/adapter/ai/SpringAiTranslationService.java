package io.github.mzahidur.i18n.infra.adapter.ai;

import io.github.mzahidur.i18n.domain.port.AiTranslationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Spring AI implementation of {@link AiTranslationPort}.
 *
 * <p>Active when {@code i18n.db.ai.provider=spring-ai} and the
 * {@code spring-ai-openai-spring-boot-starter} (or any other Spring AI
 * provider starter) is on the classpath. The {@link ChatClient} bean is
 * supplied by Spring AI's own auto-configuration and injected into this
 * adapter by {@code AiAutoConfig}.</p>
 *
 * <h3>Prompt design</h3>
 * <p>Translates {@code sourceText} from {@code sourceLocale} into
 * {@code targetLocale}. Optional {@code context} hints (e.g.
 * {@code "domain" -> "ui_label"}, {@code "tone" -> "formal"}) are appended
 * to the prompt when present, letting callers disambiguate short UI strings.
 * The model is instructed to return only the translated text.</p>
 *
 * <h3>Error handling</h3>
 * <p>Any exception is caught and logged; the method returns
 * {@link Optional#empty()} so the chain degrades gracefully rather than
 * propagating an API failure.</p>
 */
public class SpringAiTranslationService implements AiTranslationPort {

    private static final Logger log = LoggerFactory.getLogger(SpringAiTranslationService.class);

    private static final String BASE_PROMPT =
            "Translate the following text from %s (%s) into %s (%s).\n" +
            "Respond with only the translated text — no explanation, no quotes, no punctuation changes.\n\n" +
            "Text: \"%s\"";

    private final ChatClient chatClient;

    public SpringAiTranslationService(ChatClient chatClient) {
        this.chatClient = Objects.requireNonNull(chatClient, "chatClient must not be null");
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
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                log.warn("Spring AI returned blank response for sourceLocale='{}' targetLocale='{}'",
                         sourceLocale, targetLocale);
                return Optional.empty();
            }

            String translation = response.strip();
            log.debug("Spring AI translated '{}' ({} → {}) → '{}'",
                      sourceText, sourceLocale, targetLocale, translation);
            return Optional.of(translation);

        } catch (Exception ex) {
            log.error("Spring AI translation failed (sourceLocale='{}' targetLocale='{}'): {}",
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
        return "spring-ai";
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
