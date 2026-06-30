package io.github.mzahidur.i18n.infra.adapter.ai;

import io.github.mzahidur.i18n.domain.port.AiTranslationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Spring AI implementation of {@link AiTranslationPort}.
 *
 * <p>Active when {@code i18n.db.ai.provider=spring-ai} and the
 * {@code spring-ai-openai-spring-boot-starter} (or any other Spring AI
 * provider starter) is on the classpath.  The {@link ChatClient} bean is
 * supplied by Spring AI's own auto-configuration and injected into this
 * adapter by {@code AiAutoConfig}.</p>
 *
 * <h3>Prompt design</h3>
 * <p>The prompt asks for a plain translation with no surrounding explanation.
 * The model is instructed to respond with the translated string only, making
 * the response safe to store directly without post-processing.</p>
 *
 * <h3>Error handling</h3>
 * <p>Any exception from the Spring AI call is caught and logged; the method
 * returns {@link Optional#empty()} so the chain degrades gracefully rather
 * than propagating an API failure to the end user.</p>
 */
public class SpringAiTranslationService implements AiTranslationPort {

    private static final Logger log = LoggerFactory.getLogger(SpringAiTranslationService.class);

    private static final String PROMPT_TEMPLATE =
            "Translate the following UI message key into %s (%s).\n" +
            "Respond with only the translated text — no explanation, no quotes, no punctuation changes.\n\n" +
            "Message key: \"%s\"";

    private final ChatClient chatClient;

    public SpringAiTranslationService(ChatClient chatClient) {
        this.chatClient = Objects.requireNonNull(chatClient, "chatClient must not be null");
    }

    @Override
    public Optional<String> translate(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        String prompt = String.format(
                PROMPT_TEMPLATE,
                locale.getDisplayLanguage(Locale.ENGLISH),
                locale,
                code
        );

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                log.warn("Spring AI returned blank response for code='{}' locale='{}'", code, locale);
                return Optional.empty();
            }

            String translation = response.strip();
            log.debug("Spring AI translated code='{}' locale='{}' → '{}'", code, locale, translation);
            return Optional.of(translation);

        } catch (Exception ex) {
            log.error("Spring AI translation failed for code='{}' locale='{}': {}",
                      code, locale, ex.getMessage(), ex);
            return Optional.empty();
        }
    }
}
