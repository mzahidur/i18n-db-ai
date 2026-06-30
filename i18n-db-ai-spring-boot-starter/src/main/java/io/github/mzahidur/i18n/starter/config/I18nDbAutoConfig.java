package io.github.mzahidur.i18n.starter;

import io.github.mzahidur.i18n.application.service.AiTranslationProvider;
import io.github.mzahidur.i18n.application.service.DbTranslationProvider;
import io.github.mzahidur.i18n.application.service.DefaultCacheKeyResolver;
import io.github.mzahidur.i18n.application.service.PropertiesTranslationProvider;
import io.github.mzahidur.i18n.application.service.TenantAwareCacheKeyResolver;
import io.github.mzahidur.i18n.application.service.TranslationApplicationService;
import io.github.mzahidur.i18n.application.service.TranslationCacheManager;
import io.github.mzahidur.i18n.application.service.TranslationChain;
import io.github.mzahidur.i18n.application.service.TranslationProvider;
import io.github.mzahidur.i18n.domain.port.AiTranslationPort;
import io.github.mzahidur.i18n.domain.port.CacheKeyResolver;
import io.github.mzahidur.i18n.domain.port.CachePort;
import io.github.mzahidur.i18n.domain.port.PropertiesMessageSourcePort;
import io.github.mzahidur.i18n.domain.port.TenantIdResolver;
import io.github.mzahidur.i18n.domain.port.TranslationRepositoryPort;
import io.github.mzahidur.i18n.infra.adapter.jpa.I18nMessageJpaRepository;
import io.github.mzahidur.i18n.infra.adapter.jpa.JpaTranslationRepositoryAdapter;
import io.github.mzahidur.i18n.infra.messaging.DatabaseMessageSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Main auto-configuration entry point for the i18n-db-ai library.
 *
 * <p>Registered via {@code META-INF/spring/AutoConfiguration.imports} so that
 * Spring Boot picks it up automatically on the classpath without any
 * {@code @Import} in the host application.</p>
 *
 * <p>Wiring order (all guarded by {@code i18n.db.enabled=true}):</p>
 * <ol>
 *   <li>{@link CacheAutoConfig}  — binds {@link CachePort}</li>
 *   <li>{@link AiAutoConfig}     — binds {@link AiTranslationPort}</li>
 *   <li>This class               — binds everything else</li>
 * </ol>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(I18nDbProperties.class)
@Import({CacheAutoConfig.class, AiAutoConfig.class})
@ConditionalOnProperty(prefix = "i18n.db", name = "enabled", havingValue = "true", matchIfMissing = true)
public class I18nDbAutoConfig {

    // =========================================================================
    // Repository port
    // =========================================================================

    @Bean
    @ConditionalOnMissingBean(TranslationRepositoryPort.class)
    public TranslationRepositoryPort translationRepositoryPort(I18nMessageJpaRepository jpaRepository) {
        return new JpaTranslationRepositoryAdapter(jpaRepository);
    }

    // =========================================================================
    // Properties message source port
    // =========================================================================

    @Bean
    @ConditionalOnMissingBean(PropertiesMessageSourcePort.class)
    public PropertiesMessageSourcePort propertiesMessageSourcePort(I18nDbProperties properties) {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename(properties.getProperties().getBasename());
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(false);

        // Wrap in an adapter that converts Spring's NoSuchMessageException → Optional.empty()
        return (code, locale) -> {
            try {
                return Optional.of(source.getMessage(code, null, locale));
            } catch (org.springframework.context.NoSuchMessageException ex) {
                return Optional.empty();
            }
        };
    }

    // =========================================================================
    // Cache key resolver
    // =========================================================================

    @Bean
    @ConditionalOnMissingBean(CacheKeyResolver.class)
    public CacheKeyResolver cacheKeyResolver(I18nDbProperties properties,
                                              BeanFactory beanFactory) {
        String strategy = properties.getCache().getKeyResolver();

        return switch (strategy) {
            case "default" -> new DefaultCacheKeyResolver();
            case "tenant-aware" -> {
                TenantIdResolver tenantIdResolver = beanFactory.getBean(
                        properties.getTenant().getResolver(), TenantIdResolver.class);
                yield new TenantAwareCacheKeyResolver(tenantIdResolver);
            }
            default -> {
                // Treat as a custom bean name registered by the host application
                yield beanFactory.getBean(strategy, CacheKeyResolver.class);
            }
        };
    }

    // =========================================================================
    // Fallback chain providers
    // =========================================================================

    @Bean
    @ConditionalOnMissingBean(DbTranslationProvider.class)
    public TranslationProvider dbTranslationProvider(TranslationRepositoryPort repository) {
        return new DbTranslationProvider(repository);
    }

    @Bean
    @ConditionalOnMissingBean(PropertiesTranslationProvider.class)
    public TranslationProvider propertiesTranslationProvider(PropertiesMessageSourcePort messageSourcePort) {
        return new PropertiesTranslationProvider(messageSourcePort);
    }

    @Bean
    @ConditionalOnMissingBean(AiTranslationProvider.class)
    public TranslationProvider aiTranslationProvider(AiTranslationPort aiPort,
                                                      TranslationRepositoryPort repository,
                                                      I18nDbProperties properties) {
        return new AiTranslationProvider(aiPort, repository, properties.getAi().isStoreResult());
    }

    // =========================================================================
    // Chain + Application service
    // =========================================================================

    @Bean
    @ConditionalOnMissingBean(TranslationChain.class)
    public TranslationChain translationChain(List<TranslationProvider> providers) {
        return new TranslationChain(providers);
    }

    @Bean
    @ConditionalOnMissingBean(TranslationApplicationService.class)
    public TranslationApplicationService translationApplicationService(TranslationChain chain,
                                                                        CachePort cachePort,
                                                                        CacheKeyResolver keyResolver) {
        return new TranslationApplicationService(chain, cachePort, keyResolver);
    }

    // =========================================================================
    // Cache manager (exposed for host application injection)
    // =========================================================================

    @Bean
    @ConditionalOnMissingBean(TranslationCacheManager.class)
    public TranslationCacheManager translationCacheManager(CachePort cachePort,
                                                            CacheKeyResolver keyResolver) {
        return new TranslationCacheManager(cachePort, keyResolver);
    }

    // =========================================================================
    // DatabaseMessageSource — @Primary Spring MessageSource
    // =========================================================================

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "databaseMessageSource")
    public MessageSource databaseMessageSource(TranslationApplicationService service) {
        return new DatabaseMessageSource(service);
    }

    // =========================================================================
    // Flyway migration
    // =========================================================================

    @Bean(initMethod = "migrate")
    @ConditionalOnClass(Flyway.class)
    @ConditionalOnProperty(prefix = "i18n.db.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "i18nFlyway")
    public Flyway i18nFlyway(DataSource dataSource, I18nDbProperties properties) {
        String vendor = resolveVendor(dataSource, properties.getFlyway().getVendor());
        String location = vendorToLocation(vendor);

        return Flyway.configure()
                .dataSource(dataSource)
                .locations(location)
                .table("i18n_flyway_schema_history")   // isolated from host app's Flyway history
                .baselineOnMigrate(true)
                .load();
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    private String resolveVendor(DataSource dataSource, String configured) {
        if (!"auto".equalsIgnoreCase(configured)) {
            return configured.toLowerCase();
        }
        // Auto-detect from JDBC URL
        try (java.sql.Connection conn = dataSource.getConnection()) {
            String url = conn.getMetaData().getURL().toLowerCase();
            if (url.contains("mysql") || url.contains("mariadb")) return "mysql";
            if (url.contains("oracle"))                            return "oracle";
            if (url.contains("postgresql") || url.contains("postgres")) return "postgresql";
            if (url.contains("sqlserver") || url.contains("microsoft")) return "sqlserver";
            return "h2"; // H2 / ANSI fallback
        } catch (Exception ex) {
            return "h2";
        }
    }

    private String vendorToLocation(String vendor) {
        return switch (vendor) {
            case "mysql"      -> "classpath:db/migration/mysql";
            case "oracle"     -> "classpath:db/migration/oracle";
            case "postgresql" -> "classpath:db/migration/postgresql";
            case "sqlserver"  -> "classpath:db/migration/sqlserver";
            default           -> "classpath:db/migration";  // root = ANSI / H2
        };
    }
}
