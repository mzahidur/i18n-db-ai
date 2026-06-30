package io.github.mzahidur.i18n.starter.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.mzahidur.i18n.domain.port.AiTranslationPort;
import io.github.mzahidur.i18n.infra.adapter.ai.LangChain4jTranslationService;
import io.github.mzahidur.i18n.infra.adapter.ai.NoOpAiTranslationService;
import io.github.mzahidur.i18n.infra.adapter.ai.SpringAiTranslationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the {@link AiTranslationPort} binding.
 *
 * <p>The active provider is selected by {@code i18n.db.ai.provider}:</p>
 * <ul>
 *   <li>{@code spring-ai}   — {@link SpringAiTranslationService} (requires Spring AI on classpath)</li>
 *   <li>{@code langchain4j} — {@link LangChain4jTranslationService} (requires LangChain4j on classpath)</li>
 *   <li>{@code none} / absent / AI disabled — {@link NoOpAiTranslationService} (safe default)</li>
 * </ul>
 *
 * <p>This class only constructs provider implementations and never calls
 * {@link AiTranslationPort#translate} directly, so it is unaffected by the
 * {@code translate(sourceText, sourceLocale, targetLocale, context)} signature
 * used in the current port — that contract is exercised inside
 * {@code AiTranslationProvider} (application layer), not here.</p>
 *
 * <p>Each provider block is guarded by both a {@code @ConditionalOnClass} (the
 * provider library must be on the classpath) and a {@code @ConditionalOnProperty}
 * (the host application must explicitly opt in).</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "i18n.db", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AiAutoConfig {

    // -------------------------------------------------------------------------
    // Spring AI
    // -------------------------------------------------------------------------

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ChatClient.class)
    @ConditionalOnProperty(prefix = "i18n.db.ai", name = "provider", havingValue = "spring-ai")
    static class SpringAiConfig {

        @Bean
        @ConditionalOnMissingBean(AiTranslationPort.class)
        public AiTranslationPort springAiTranslationPort(ChatClient.Builder builder) {
            return new SpringAiTranslationService(builder.build());
        }
    }

    // -------------------------------------------------------------------------
    // LangChain4j
    // -------------------------------------------------------------------------

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ChatLanguageModel.class)
    @ConditionalOnProperty(prefix = "i18n.db.ai", name = "provider", havingValue = "langchain4j")
    static class LangChain4jConfig {

        @Bean
        @ConditionalOnMissingBean(AiTranslationPort.class)
        public AiTranslationPort langChain4jTranslationPort(ChatLanguageModel model) {
            return new LangChain4jTranslationService(model);
        }
    }

    // -------------------------------------------------------------------------
    // No-op fallback (default when ai.enabled=false or provider=none)
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(AiTranslationPort.class)
    public AiTranslationPort noOpAiTranslationPort() {
        return new NoOpAiTranslationService();
    }
}