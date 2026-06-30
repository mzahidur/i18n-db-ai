package io.github.mzahidur.i18n.application.service.provider;

import io.github.mzahidur.i18n.domain.port.PropertiesMessageSourcePort;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Second provider in the fallback chain.
 *
 * <p>Delegates to {@link PropertiesMessageSourcePort}, which is implemented in
 * the infrastructure layer by a wrapper around Spring's
 * {@code ResourceBundleMessageSource}.  This provider is intentionally
 * framework-agnostic at the application layer: it knows nothing about Spring.</p>
 *
 * <p>Returns {@link Optional#empty()} when the properties files do not contain
 * the requested code/locale combination, passing control to the AI provider.</p>
 */
public class PropertiesTranslationProvider implements TranslationProvider {

    private static final int ORDER = 20;

    private final PropertiesMessageSourcePort messageSource;

    public PropertiesTranslationProvider(PropertiesMessageSourcePort messageSource) {
        this.messageSource = Objects.requireNonNull(messageSource, "messageSource must not be null");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the message code from the classpath {@code .properties} bundle.
     * Locale resolution (language → country → variant) is delegated to the port
     * implementation.</p>
     */
    @Override
    public Optional<String> resolve(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        return messageSource.getMessage(code, locale);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public String getName() {
        return "PROPERTIES";
    }
}