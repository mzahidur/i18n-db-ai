package io.github.mzahidur.i18n.infra.adapter.ai;

import io.github.mzahidur.i18n.domain.port.AiTranslationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Null Object implementation of {@link AiTranslationPort}.
 *
 * <p>Registered as the default bean when {@code i18n.db.ai.enabled=false}
 * (the library default) or when {@code i18n.db.ai.provider=none}. Logs a
 * single WARN per call so misconfiguration is visible without throwing an
 * exception that would break the fallback chain.</p>
 *
 * <p>{@link #isAvailable()} returns {@code false}, allowing application-layer
 * callers to skip the AI step entirely rather than invoking it just to get
 * an empty result.</p>
 */
public class NoOpAiTranslationService implements AiTranslationPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpAiTranslationService.class);

    @Override
    public Optional<String> translate(String sourceText,
                                       Locale sourceLocale,
                                       Locale targetLocale,
                                       Map<String, String> context) {
        log.warn("AI translation requested for sourceLocale='{}' targetLocale='{}' but no AI " +
                 "provider is configured (i18n.db.ai.enabled=false). Returning empty.",
                 sourceLocale, targetLocale);
        return Optional.empty();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String providerName() {
        return "noop";
    }
}
