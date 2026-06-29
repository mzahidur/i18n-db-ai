package io.github.mzahidur.i18n.domain.port;

import java.util.Locale;
import java.util.Optional;

/**
 * Domain port abstracting classpath {@code .properties} bundle resolution.
 *
 * <p>The infrastructure layer implements this via a thin wrapper around
 * Spring's {@code ResourceBundleMessageSource}.  The domain and application
 * layers reference only this interface — no Spring import ever crosses the
 * boundary.</p>
 *
 * <p>Locale resolution order (language → country → variant) is entirely the
 * responsibility of the implementing adapter; callers pass the full
 * {@link Locale} and receive the best-match result or {@link Optional#empty()}.</p>
 */
public interface PropertiesMessageSourcePort {

    /**
     * Look up a message code in the classpath properties bundle.
     *
     * @param code   the message code (e.g. {@code "user.greeting"})
     * @param locale the requested locale
     * @return the resolved message, or {@link Optional#empty()} if the code
     *         is not present in any bundle variant for the given locale
     */
    Optional<String> getMessage(String code, Locale locale);
}