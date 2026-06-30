package io.github.mzahidur.i18n.infra.messaging;

import io.github.mzahidur.i18n.application.service.TranslationApplicationService;
import io.github.mzahidur.i18n.domain.exception.TranslationException;
import io.github.mzahidur.i18n.domain.port.TenantIdResolver;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Adapts {@link TranslationApplicationService} to the standard Spring
 * {@link MessageSource} interface.
 *
 * <p>Registered as a {@code @Primary} bean in {@code I18nDbAutoConfig} so it
 * takes precedence over any {@code ResourceBundleMessageSource} or other
 * {@code MessageSource} bean in the host application context. The
 * properties-file fallback is handled internally within the application
 * layer's fallback chain, not by delegating to a secondary
 * {@code MessageSource}.</p>
 *
 * <h3>Tenancy</h3>
 * <p>{@link TenantIdResolver} is optional ({@code @Nullable}). When present
 * (i.e. {@code i18n.db.tenant.enabled=true} and a resolver bean is
 * configured), the current tenant ID is resolved on every call and passed
 * through to {@link TranslationApplicationService#getMessage(String, Locale, String)}.
 * When absent, the single-tenant overload is used.</p>
 *
 * <h3>Argument substitution</h3>
 * <p>When {@code args} are supplied, the resolved string is treated as a
 * {@link MessageFormat} pattern and formatted before returning, mirroring
 * Spring's built-in message sources.</p>
 */
public class DatabaseMessageSource implements MessageSource {

    private final TranslationApplicationService translationService;
    private final TenantIdResolver tenantIdResolver;

    /**
     * @param translationService the core translation use-case orchestrator (required)
     * @param tenantIdResolver   optional tenant resolver; {@code null} for single-tenant mode
     */
    public DatabaseMessageSource(TranslationApplicationService translationService,
                                 @Nullable TenantIdResolver tenantIdResolver) {
        this.translationService = Objects.requireNonNull(translationService,
                "translationService must not be null");
        this.tenantIdResolver = tenantIdResolver;
    }

    // -------------------------------------------------------------------------
    // MessageSource
    // -------------------------------------------------------------------------

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        try {
            String resolved = resolve(code, locale);
            return format(resolved, args, locale);
        } catch (TranslationException ex) {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        try {
            String resolved = resolve(code, locale);
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
                    String resolved = resolve(code, locale);
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

    /**
     * Resolves a message code, transparently routing through the tenant-aware
     * overload when a {@link TenantIdResolver} is configured.
     */
    private String resolve(String code, Locale locale) {
        if (tenantIdResolver != null) {
            String tenantId = tenantIdResolver.currentTenantId();
            return translationService.getMessage(code, locale, tenantId);
        }
        return translationService.getMessage(code, locale);
    }

    private String format(String pattern, Object[] args, Locale locale) {
        if (args == null || args.length == 0) {
            return pattern;
        }
        // MessageFormat is not thread-safe — create per call (same as Spring's AbstractMessageSource)
        return new MessageFormat(pattern, locale).format(args);
    }
}
