package io.github.mzahidur.i18n.domain.port;

import io.github.mzahidur.i18n.domain.model.I18nMessage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Primary port for persisting and retrieving {@link I18nMessage} records.
 *
 * <p>This interface is the domain's contract for storage — it knows nothing
 * about JPA, JDBC, or any persistence framework. The infrastructure layer
 * provides the adapter ({@code JpaTranslationRepositoryAdapter}).</p>
 *
 * <p>All lookup methods are tenant-aware: pass {@code null} as {@code tenantId}
 * to operate in single-tenant mode.</p>
 */
public interface TranslationRepositoryPort {

    // ── Reads ─────────────────────────────────────────────────────────────────

    /**
     * Finds a translation by its message key and locale.
     *
     * @param code      the message key (e.g. {@code "user.welcome.title"})
     * @param locale    the BCP-47 locale tag (e.g. {@code "en-US"})
     * @param tenantId  tenant identifier; {@code null} for single-tenant
     * @return the matching message, or {@link Optional#empty()} if not found
     */
    Optional<I18nMessage> findByCodeAndLocale(String code, String locale, String tenantId);

    /**
     * Finds all translations for a given locale, optionally scoped to a tenant.
     *
     * @param locale    the BCP-47 locale tag
     * @param tenantId  tenant identifier; {@code null} for single-tenant
     * @return list of matching messages (may be empty, never null)
     */
    List<I18nMessage> findAllByLocale(String locale, String tenantId);

    /**
     * Finds all translations for a given message key across all locales.
     *
     * @param code      the message key
     * @param tenantId  tenant identifier; {@code null} for single-tenant
     * @return list of matching messages (may be empty, never null)
     */
    List<I18nMessage> findAllByCode(String code, String tenantId);

    /**
     * Returns {@code true} if a translation exists for the given key and locale.
     *
     * @param code      the message key
     * @param locale    the BCP-47 locale tag
     * @param tenantId  tenant identifier; {@code null} for single-tenant
     */
    boolean existsByCodeAndLocale(String code, String locale, String tenantId);

    // ── Writes ────────────────────────────────────────────────────────────────

    /**
     * Persists a new translation or updates an existing one (upsert semantics).
     *
     * <p>If a record with the same {@code (code, locale, tenantId)} already
     * exists, its {@code message} and audit metadata are updated.</p>
     *
     * @param message the translation to save
     * @return the saved (and possibly id-populated) translation
     */
    I18nMessage save(I18nMessage message);

    /**
     * Batch-saves a collection of translations.
     * Implementations should optimise for bulk insert/update.
     *
     * @param messages the translations to save
     * @return saved translations in the same order as input
     */
    List<I18nMessage> saveAll(Collection<I18nMessage> messages);

    /**
     * Deletes the translation identified by the given key, locale, and tenant.
     * No-op if the record does not exist.
     *
     * @param code      the message key
     * @param locale    the BCP-47 locale tag
     * @param tenantId  tenant identifier; {@code null} for single-tenant
     */
    void deleteByCodeAndLocale(String code, String locale, String tenantId);

    /**
     * Deletes all translations for a given tenant.
     * Useful for tenant offboarding.
     *
     * @param tenantId the tenant whose translations should be removed
     */
    void deleteAllByTenantId(String tenantId);
}
