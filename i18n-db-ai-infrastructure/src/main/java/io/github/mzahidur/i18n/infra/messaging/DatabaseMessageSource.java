package io.github.mzahidur.i18n.infra.messaging;

import io.github.mzahidur.i18n.application.service.TranslationApplicationService;
import io.github.mzahidur.i18n.domain.exception.TranslationException;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Adapts {@link TranslationApplicationService} to the standard Spring
 * {@link MessageSource} interface.
 *
 * <p>Registered as a {@code @Primary} bean in {@code I18nDbAutoConfig} so that
 * it takes precedence over any {@code ResourceBundleMessageSource} or other
 * {@code MessageSource} bean present in the host application context.  The
 * properties-file fallback is handled internally within the application layer
 * chain, not by delegating to a secondary {@code MessageSource}.</p>
 *
 * <h3>Argument substitution</h3>
 * <p>When {@code args} are supplied, the resolved string is treated as a
 * {@link MessageFormat} pattern and formatted before returning.  This mirrors
 * the behaviour of Spring's built-in message sources.</p>
 *
 * <h3>{@link MessageSourceResolvable}</h3>
 * <p>Iterates the resolvable's codes in order and returns the first hit.
 * Falls back to the default message if all codes miss and a default is set;
 * otherwise throws {@link NoSuchMessageException}.</p>
 */
public class DatabaseMessageSource implements MessageSource {

    private final TranslationApplicationService translationService;

    private final TranslationApplicationService translationService;
    private final TenantIdResolver tenantIdResolver;

    public DatabaseMessageSource(
            TranslationApplicationService translationService,
            @Nullable TenantIdResolver tenantIdResolver
    ) {
        this.translationService = Objects.requireNonNull(translationService);
        this.tenantIdResolver   = tenantIdResolver;
    }

    // -------------------------------------------------------------------------
    // MessageSource
    // -------------------------------------------------------------------------

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        try {
            String tenantId = (tenantIdResolver != null) ? tenantIdResolver.resolve() : null;
            String resolved = translationService.getMessage(code, locale, tenantId);
            return format(resolved, args, locale);
        } catch (TranslationException ex) {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        try {
            String tenantId = (tenantIdResolver != null) ? tenantIdResolver.resolve() : null;
            String resolved = translationService.getMessage(code, locale, tenantId);
            return format(resolved, args, locale);
        } catch (TranslationException ex) {
            throw new NoSuchMessageException(code, locale);
        }
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();

        if (codes != null) {
            for (String code : codes) {
                try {
                    String tenantId = (tenantIdResolver != null) ? tenantIdResolver.resolve() : null;
                    String resolved = translationService.getMessage(code, locale, tenantId);
                    return format(resolved, resolvable.getArguments(), locale);
                } catch (TranslationException ex) {
                    // try next code
                }
            }
        }

        // All codes exhausted — try the resolvable's own default
        String defaultMessage = resolvable.getDefaultMessage();
        if (defaultMessage != null) {
            return format(defaultMessage, resolvable.getArguments(), locale);
        }

        String firstCode = (codes != null && codes.length > 0) ? codes[0] : "(none)";
        throw new NoSuchMessageException(firstCode, locale);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String format(String pattern, Object[] args, Locale locale) {
        if (args == null || args.length == 0) {
            return pattern;
        }
        // MessageFormat is not thread-safe — create per call (same as Spring's AbstractMessageSource)
        return new MessageFormat(pattern, locale).format(args);
    }
}
