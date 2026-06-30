package io.github.mzahidur.i18n.infra.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link I18nMessageJpaEntity}.
 *
 * <p>All query methods are expressed in JPQL to remain database-vendor agnostic.
 * Native SQL is intentionally avoided so that the same repository works against
 * H2 (tests), MySQL, PostgreSQL, Oracle, and SQL Server without modification.</p>
 */
public interface I18nMessageJpaRepository extends JpaRepository<I18nMessageJpaEntity, Long> {

    /**
     * Exact lookup by message code and locale string.
     *
     * <p>The locale is stored as {@link java.util.Locale#toString()} (e.g.
     * {@code "en_US"}) by the adapter layer.</p>
     *
     * @param code   message code
     * @param locale locale string
     * @return the matching entity, or empty if not found
     */
    Optional<I18nMessageJpaEntity> findByCodeAndLocale(String code, String locale);

    /**
     * Lookup by code and language-only locale root (e.g. {@code "en"} as
     * fallback when {@code "en_US"} is not stored).
     *
     * <p>Used by {@link JpaTranslationRepositoryAdapter} as a second-pass
     * fallback after an exact locale miss.</p>
     *
     * @param code     message code
     * @param language ISO 639 language code (no country/variant)
     * @return the matching entity, or empty if not found
     */
    @Query("SELECT m FROM I18nMessageJpaEntity m WHERE m.code = :code AND m.locale = :language")
    Optional<I18nMessageJpaEntity> findByCodeAndLanguage(@Param("code") String code,
                                                          @Param("language") String language);

    /**
     * Check existence without loading the full entity — used by
     * {@link JpaTranslationRepositoryAdapter#exists(String, java.util.Locale)}.
     *
     * @param code   message code
     * @param locale locale string
     * @return {@code true} if a row exists
     */
    boolean existsByCodeAndLocale(String code, String locale);

    /**
     * Delete all entries for a given message code across all locales.
     * Useful for bulk eviction when a message key is renamed or removed.
     *
     * @param code message code to purge
     */
    void deleteByCode(String code);
}
