package io.github.mzahidur.i18n.application.service.chain;

import io.github.mzahidur.i18n.application.service.provider.AiTranslationProvider;
import io.github.mzahidur.i18n.application.service.provider.DbTranslationProvider;
import io.github.mzahidur.i18n.application.service.provider.PropertiesTranslationProvider;
import io.github.mzahidur.i18n.application.service.provider.TranslationProvider;
import io.github.mzahidur.i18n.domain.exception.TranslationException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Chain of Responsibility executor for the translation fallback pipeline.
 *
 * <p>Iterates over all registered {@link TranslationProvider} instances in
 * ascending {@link TranslationProvider#getOrder()} order and returns the first
 * non-empty result.  If no provider can fulfil the request, a
 * {@link TranslationException.TranslationNotFoundException} is thrown.</p>
 *
 * <p>This class is intentionally stateless beyond the immutable provider list —
 * it is safe for concurrent use without synchronisation.</p>
 *
 * <h3>Example provider ordering (defaults)</h3>
 * <ol>
 *   <li>order=10 → {@link DbTranslationProvider}</li>
 *   <li>order=20 → {@link PropertiesTranslationProvider}</li>
 *   <li>order=30 → {@link AiTranslationProvider}</li>
 * </ol>
 */
public class TranslationChain {

    private final List<TranslationProvider> providers;

    /**
     * @param providers all providers that participate in the chain; must not be null or empty
     */
    public TranslationChain(List<TranslationProvider> providers) {
        Objects.requireNonNull(providers, "providers must not be null");
        if (providers.isEmpty()) {
            throw new IllegalArgumentException("At least one TranslationProvider must be registered");
        }
        // Sort once at construction time — providers list is immutable afterwards
        this.providers = providers.stream()
                .sorted(Comparator.comparingInt(TranslationProvider::getOrder))
                .toList();
    }

    /**
     * Resolve a translation by walking the chain in order.
     *
     * @param code   message code
     * @param locale target locale
     * @return the resolved translation string
     * @throws TranslationException.TranslationNotFoundException if no provider could supply a translation
     */
    public String resolve(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        for (TranslationProvider provider : providers) {
            Optional<String> result = provider.resolve(code, locale);
            if (result.isPresent()) {
                return result.get();
            }
        }

        throw new TranslationException.TranslationNotFoundException(
                code, locale.toString(), null);
    }

    /**
     * Returns an unmodifiable view of the sorted provider list, useful for
     * diagnostics and health-check endpoints.
     *
     * @return sorted list of providers
     */
    public List<TranslationProvider> getProviders() {
        return providers; // already unmodifiable (List.of / stream().toList())
    }
}
