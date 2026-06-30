package io.github.mzahidur.i18n.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Type-safe binding for all {@code i18n.db.*} configuration properties.
 *
 * <p>Bound automatically by Spring Boot when this class is registered as a
 * {@code @ConfigurationProperties} bean in {@code I18nDbAutoConfig}.  Host
 * applications configure the library exclusively through {@code application.yml}
 * or {@code application.properties} — no programmatic configuration API is needed.</p>
 *
 * <h3>Minimal configuration (defaults only)</h3>
 * <pre>
 * # Nothing required — the library works out of the box with DB + Properties fallback.
 * </pre>
 *
 * <h3>Full configuration example</h3>
 * <pre>
 * i18n:
 *   db:
 *     enabled: true
 *     cache:
 *       type: caffeine          # auto | caffeine | redis | none
 *       ttl: 1h
 *       ttl-per-locale:
 *         en: 2h
 *         bn: 30m
 *       key-resolver: default   # default | tenant-aware | &lt;bean name&gt;
 *     ai:
 *       enabled: true
 *       provider: spring-ai     # none | spring-ai | langchain4j
 *       store-result: true
 *     flyway:
 *       enabled: true
 *       vendor: auto            # auto | mysql | oracle | postgresql | sqlserver | h2
 *     tenant:
 *       enabled: false
 *       resolver:               # bean name of TenantIdResolver impl
 *     properties:
 *       basename: messages
 * </pre>
 */
@ConfigurationProperties(prefix = "i18n.db")
public class I18nDbProperties {

    /** Master switch. Set to {@code false} to disable the library entirely. */
    private boolean enabled = true;

    private final Cache cache = new Cache();
    private final Ai ai = new Ai();
    private final Flyway flyway = new Flyway();
    private final Tenant tenant = new Tenant();
    private final MessageSourceProperties properties = new MessageSourceProperties();

    // -------------------------------------------------------------------------
    // Nested: Cache
    // -------------------------------------------------------------------------

    public static class Cache {

        /**
         * Cache backend selection.
         * <ul>
         *   <li>{@code auto}     — Caffeine if on classpath, else Redis, else none</li>
         *   <li>{@code caffeine} — force Caffeine (fails if not on classpath)</li>
         *   <li>{@code redis}    — force Redis (fails if not on classpath)</li>
         *   <li>{@code none}     — disable caching entirely</li>
         * </ul>
         */
        private String type = "auto";

        /** Default TTL applied to all cache entries. */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration ttl = Duration.ofHours(1);

        /**
         * Per-locale TTL overrides. Key = locale string (e.g. {@code "en_US"}),
         * value = duration. Locales not listed here use {@link #ttl}.
         */
        private Map<String, Duration> ttlPerLocale = new HashMap<>();

        /**
         * Cache key resolver strategy.
         * <ul>
         *   <li>{@code default}       — {@code {code}:{locale}}</li>
         *   <li>{@code tenant-aware}  — {@code {tenantId}:{code}:{locale}}</li>
         *   <li>any other string      — treated as a Spring bean name</li>
         * </ul>
         */
        private String keyResolver = "default";

        public String getType()                             { return type; }
        public void setType(String type)                    { this.type = type; }

        public Duration getTtl()                             { return ttl; }
        public void setTtl(Duration ttl)                     { this.ttl = ttl; }

        public Map<String, Duration> getTtlPerLocale()       { return ttlPerLocale; }
        public void setTtlPerLocale(Map<String, Duration> m) { this.ttlPerLocale = m; }

        public String getKeyResolver()                       { return keyResolver; }
        public void setKeyResolver(String keyResolver)       { this.keyResolver = keyResolver; }
    }

    // -------------------------------------------------------------------------
    // Nested: AI
    // -------------------------------------------------------------------------

    public static class Ai {

        /** Master switch for AI fallback. Requires a provider dependency on the classpath. */
        private boolean enabled = false;

        /**
         * AI provider implementation.
         * <ul>
         *   <li>{@code none}        — {@code NoOpAiTranslationService} (safe default)</li>
         *   <li>{@code spring-ai}   — {@code SpringAiTranslationService}</li>
         *   <li>{@code langchain4j} — {@code LangChain4jTranslationService}</li>
         * </ul>
         */
        private String provider = "none";

        /**
         * When {@code true}, AI-generated translations are persisted to the DB
         * so subsequent requests are served from the fast path.
         */
        private boolean storeResult = true;

        public boolean isEnabled()                          { return enabled; }
        public void setEnabled(boolean enabled)             { this.enabled = enabled; }

        public String getProvider()                          { return provider; }
        public void setProvider(String provider)            { this.provider = provider; }

        public boolean isStoreResult()                       { return storeResult; }
        public void setStoreResult(boolean storeResult)     { this.storeResult = storeResult; }
    }

    // -------------------------------------------------------------------------
    // Nested: Flyway
    // -------------------------------------------------------------------------

    public static class Flyway {

        /** When {@code false}, the library's bundled Flyway migrations are skipped. */
        private boolean enabled = true;

        /**
         * Database vendor for migration script selection.
         * <ul>
         *   <li>{@code auto}        — detected from the datasource URL</li>
         *   <li>{@code mysql}       — {@code db/migration/mysql/}</li>
         *   <li>{@code oracle}      — {@code db/migration/oracle/}</li>
         *   <li>{@code postgresql}  — {@code db/migration/postgresql/}</li>
         *   <li>{@code sqlserver}   — {@code db/migration/sqlserver/}</li>
         *   <li>{@code h2}          — {@code db/migration/h2/}</li>
         * </ul>
         */
        private String vendor = "auto";

        public boolean isEnabled()                          { return enabled; }
        public void setEnabled(boolean enabled)             { this.enabled = enabled; }

        public String getVendor()                            { return vendor; }
        public void setVendor(String vendor)                { this.vendor = vendor; }
    }

    // -------------------------------------------------------------------------
    // Nested: Tenant
    // -------------------------------------------------------------------------

    public static class Tenant {

        /** Enable multi-tenant cache key isolation. */
        private boolean enabled = false;

        /**
         * Spring bean name of the {@code TenantIdResolver} implementation
         * supplied by the host application.
         */
        private String resolver = "";

        public boolean isEnabled()                          { return enabled; }
        public void setEnabled(boolean enabled)             { this.enabled = enabled; }

        public String getResolver()                          { return resolver; }
        public void setResolver(String resolver)            { this.resolver = resolver; }
    }

    // -------------------------------------------------------------------------
    // Nested: MessageSourceProperties
    // -------------------------------------------------------------------------
    // NOTE: Deliberately NOT named "Properties" — java.util.Properties is a
    // core JDK class and a nested class of the same simple name creates
    // ambiguity for both the compiler and IDE tooling. Renamed for clarity.

    public static class MessageSourceProperties {

        /**
         * Base name of the classpath {@code .properties} bundle used as the
         * second-tier fallback (after DB, before AI).
         * Matches Spring's {@code spring.messages.basename} convention.
         */
        private String basename = "messages";

        public String getBasename()                          { return basename; }
        public void setBasename(String basename)            { this.basename = basename; }
    }

    // -------------------------------------------------------------------------
    // Root getters / setters
    // -------------------------------------------------------------------------

    public boolean isEnabled()                  { return enabled; }
    public void setEnabled(boolean enabled)     { this.enabled = enabled; }

    public Cache getCache()                     { return cache; }
    public Ai getAi()                           { return ai; }
    public Flyway getFlyway()                   { return flyway; }
    public Tenant getTenant()                   { return tenant; }
    public MessageSourceProperties getProperties() { return properties; }
}