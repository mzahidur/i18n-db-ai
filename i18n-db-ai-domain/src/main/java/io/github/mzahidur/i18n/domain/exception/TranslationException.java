package io.github.mzahidur.i18n.domain.exception;

/**
 * Root exception for the i18n-db-ai library.
 *
 * <p>All library-specific exceptions extend this class, allowing host
 * applications to catch the entire hierarchy with a single handler if needed.</p>
 *
 * <p>Unchecked by design — translation failures should not force callers to
 * handle checked exceptions, consistent with Spring's exception philosophy.</p>
 */
public class TranslationException extends RuntimeException {

    public TranslationException(String message) {
        super(message);
    }

    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }

    // ── Subtypes ──────────────────────────────────────────────────────────────

    /**
     * Thrown when no translation can be found through any provider in the
     * fallback chain and no default message was supplied.
     */
    public static class TranslationNotFoundException extends TranslationException {

        private final String code;
        private final String locale;
        private final String tenantId;

        public TranslationNotFoundException(String code, String locale, String tenantId) {
            super(buildMessage(code, locale, tenantId));
            this.code     = code;
            this.locale   = locale;
            this.tenantId = tenantId;
        }

        public String getCode()     { return code; }
        public String getLocale()   { return locale; }
        public String getTenantId() { return tenantId; }

        private static String buildMessage(String code, String locale, String tenantId) {
            String tenant = (tenantId != null) ? " (tenant: " + tenantId + ")" : "";
            return "No translation found for code '%s' in locale '%s'%s"
                .formatted(code, locale, tenant);
        }
    }

    /**
     * Thrown when the AI translation provider encounters an unrecoverable error
     * (network failure, rate limit exceeded, invalid response, etc.).
     *
     * <p>The fallback chain catches this and treats it as a cache miss —
     * it is not propagated to the caller unless all providers fail.</p>
     */
    public static class AiTranslationException extends TranslationException {

        private final String providerName;

        public AiTranslationException(String providerName, String message) {
            super("[%s] %s".formatted(providerName, message));
            this.providerName = providerName;
        }

        public AiTranslationException(String providerName, String message, Throwable cause) {
            super("[%s] %s".formatted(providerName, message), cause);
            this.providerName = providerName;
        }

        public String getProviderName() { return providerName; }
    }

    /**
     * Thrown when the cache backend encounters an unrecoverable error.
     *
     * <p>Cache errors must never prevent translation resolution — implementations
     * of {@link io.github.mzahidur.i18n.domain.port.CachePort} should catch
     * backend errors internally and degrade gracefully. This exception is
     * reserved for cases where degradation is not possible (e.g. cache init failure).</p>
     */
    public static class CacheException extends TranslationException {

        private final String backendName;

        public CacheException(String backendName, String message) {
            super("[%s] %s".formatted(backendName, message));
            this.backendName = backendName;
        }

        public CacheException(String backendName, String message, Throwable cause) {
            super("[%s] %s".formatted(backendName, message), cause);
            this.backendName = backendName;
        }

        public String getBackendName() { return backendName; }
    }

    /**
     * Thrown when a translation record fails domain validation
     * (e.g. blank code, unsupported locale format).
     */
    public static class TranslationValidationException extends TranslationException {

        private final String field;
        private final Object rejectedValue;

        public TranslationValidationException(String field, Object rejectedValue, String reason) {
            super("Validation failed for field '%s' (value: '%s'): %s"
                .formatted(field, rejectedValue, reason));
            this.field         = field;
            this.rejectedValue = rejectedValue;
        }

        public String getField()         { return field; }
        public Object getRejectedValue() { return rejectedValue; }
    }
}
