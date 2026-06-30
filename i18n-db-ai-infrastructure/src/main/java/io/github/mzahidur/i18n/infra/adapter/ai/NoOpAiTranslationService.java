package io.github.mzahidur.i18n.infra.adapter.ai;

import io.github.mzahidur.i18n.domain.port.AiTranslationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;

/**
 * Null Object implementation of {@link AiTranslationPort}.
 *
 * <p>Registered as the default bean when {@code i18n.db.ai.enabled=false}
 * (the library default) or when {@code i18n.db.ai.provider=none}.  Logs a
 * single WARN on each call so that misconfiguration is visible without
 * throwing an exception that would break the fallback chain.</p>
 *
 * <p>Returning {@link Optional#empty()} causes {@link
 * io.github.mzahidur.i18n.application.service.AiTranslationProvider} to
 * pass through, and the chain ends with a
 * {@link io.github.mzahidur.i18n.domain.exception.TranslationNotFoundException}.</p>
 */
public class NoOpAiTranslationService implements AiTranslationPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpAiTranslationService.class);

    @Override
    public Optional<String> translate(String code, Locale locale) {
        log.warn("AI translation requested for code='{}' locale='{}' but no AI provider is configured " +
                 "(i18n.db.ai.enabled=false). Returning empty.", code, locale);
        return Optional.empty();
    }
}
