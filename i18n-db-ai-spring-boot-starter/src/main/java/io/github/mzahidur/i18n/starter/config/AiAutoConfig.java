package io.github.mzahidur.i18n.starter.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.mzahidur.i18n.domain.port.AiTranslationPort;
import io.github.mzahidur.i18n.infra.adapter.ai.LangChain4jTranslationService;
import io.github.mzahidur.i18n.infra.adapter.ai.NoOpAiTranslationService;
import io.github.mzahidur.i18n.infra.adapter.ai.SpringAiTranslationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "i18n.db", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AiAutoConfig {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ChatClient.class)
    @ConditionalOnExpression("""
            '${i18n.db.ai.enabled:true}' == 'true' && '${i18n.db.ai.provider}' == 'spring-ai'
            """)
    static class SpringAiConfig {

        @Bean
        @ConditionalOnMissingBean(AiTranslationPort.class)
        public AiTranslationPort springAiTranslationPort(ChatClient.Builder builder) {
            return new SpringAiTranslationService(builder.build());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ChatLanguageModel.class)
    @ConditionalOnExpression("""
            '${i18n.db.ai.enabled:true}' == 'true' && '${i18n.db.ai.provider}' == 'langchain4j'
            """)
    static class LangChain4jConfig {

        @Bean
        @ConditionalOnMissingBean(AiTranslationPort.class)
        public AiTranslationPort langChain4jTranslationPort(ChatLanguageModel model) {
            return new LangChain4jTranslationService(model);
        }
    }

    @Bean
    @ConditionalOnMissingBean(AiTranslationPort.class)
    public AiTranslationPort noOpAiTranslationPort() {
        return new NoOpAiTranslationService();
    }
}