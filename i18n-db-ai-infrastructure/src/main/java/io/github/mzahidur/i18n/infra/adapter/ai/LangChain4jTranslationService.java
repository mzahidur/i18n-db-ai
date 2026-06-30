package io.github.mzahidur.i18n.infra.adapter.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
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
 * model starter) is on the classpath.  The {@link ChatLanguageModel} bean is
 * supplied by LangChain4j's own auto-configuration and injected by
 * {@code AiAutoConfig}.</p>
 *
 * <h3>Prompt design</h3>
 * <p>Uses LangChain4j's {@link PromptTemplate} with named variables for clean
 * separation of template from runtime values.  The model is instructed to
 * return only the translated string — no preamble — so the response can be
 * stored verbatim.</p>
 *
 * <h3>Error handling</h3>
 * <p>Any exception is caught and logged; the method returns
 * {@link Optional#empty()} so the fallback chain degrades gracefully.</p>
 */
public class LangChain4jTranslationService implements AiTranslationPort {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jTranslationService.class);

    private static final PromptTemplate TEMPLATE = PromptTemplate.from(
            "Translate the following UI message key into {{language}} ({{locale}}).\n" +
            "Respond with only the translated text — no explanation, no quotes, no punctuation changes.\n\n" +
            "Message key: \"{{code}}\""
    );

    private final ChatLanguageModel model;

    public LangChain4jTranslationService(ChatLanguageModel model) {
        this.model = Objects.requireNonNull(model, "model must not be null");
    }

    @Override
    public Optional<String> translate(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        Prompt prompt = TEMPLATE.apply(Map.of(
                "code",     code,
                "language", locale.getDisplayLanguage(Locale.ENGLISH),
                "locale",   locale.toString()
        ));

        try {
            String response = model.generate(prompt.text());

            if (response == null || response.isBlank()) {
                log.warn("LangChain4j returned blank response for code='{}' locale='{}'", code, locale);
                return Optional.empty();
            }

            String translation = response.strip();
            log.debug("LangChain4j translated code='{}' locale='{}' → '{}'", code, locale, translation);
            return Optional.of(translation);

        } catch (Exception ex) {
            log.error("LangChain4j translation failed for code='{}' locale='{}': {}",
                      code, locale, ex.getMessage(), ex);
            return Optional.empty();
        }
    }
}
