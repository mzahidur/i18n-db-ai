package io.github.mzahidur.i18n.infra.adapter.jpa;

import io.github.mzahidur.i18n.domain.model.I18nMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link I18nMessage}.
 *
 * <p>{@link I18nMessage} is itself the {@code @Entity} (defined in the domain
 * module) — there is no separate infrastructure-layer JPA entity. This
 * repository is a thin Spring Data interface; all tenant-aware query logic
 * is expressed here so {@link JpaTranslationRepositoryAdapter} stays a pure
 * delegation layer.</p>
 *
 * <p>{@code tenantId} is nullable throughout. JPQL {@code (:tenantId IS NULL AND
 * m.tenantId IS NULL) OR m.tenantId = :tenantId} is used instead of relying on
 * {@code =} comparison, since SQL {@code NULL = NULL} is never true.</p>
 */
public interface I18nMessageJpaRepository extends JpaRepository<I18nMessage, Long> {

    @Query("""
           SELECT m FROM I18nMessage m
           WHERE m.code = :code
             AND m.locale = :locale
             AND ((:tenantId IS NULL AND m.tenantId IS NULL) OR m.tenantId = :tenantId)
           """)
    Optional<I18nMessage> findByCodeAndLocaleAndTenant(@Param("code") String code,
                                                        @Param("locale") String locale,
                                                        @Param("tenantId") String tenantId);

    @Query("""
           SELECT m FROM I18nMessage m
           WHERE m.locale = :locale
             AND ((:tenantId IS NULL AND m.tenantId IS NULL) OR m.tenantId = :tenantId)
           """)
    List<I18nMessage> findAllByLocaleAndTenant(@Param("locale") String locale,
                                                @Param("tenantId") String tenantId);

    @Query("""
           SELECT m FROM I18nMessage m
           WHERE m.code = :code
             AND ((:tenantId IS NULL AND m.tenantId IS NULL) OR m.tenantId = :tenantId)
           """)
    List<I18nMessage> findAllByCodeAndTenant(@Param("code") String code,
                                              @Param("tenantId") String tenantId);

    @Query("""
           SELECT COUNT(m) > 0 FROM I18nMessage m
           WHERE m.code = :code
             AND m.locale = :locale
             AND ((:tenantId IS NULL AND m.tenantId IS NULL) OR m.tenantId = :tenantId)
           """)
    boolean existsByCodeAndLocaleAndTenant(@Param("code") String code,
                                            @Param("locale") String locale,
                                            @Param("tenantId") String tenantId);

    @Modifying
    @Query("""
           DELETE FROM I18nMessage m
           WHERE m.code = :code
             AND m.locale = :locale
             AND ((:tenantId IS NULL AND m.tenantId IS NULL) OR m.tenantId = :tenantId)
           """)
    void deleteByCodeAndLocaleAndTenant(@Param("code") String code,
                                        @Param("locale") String locale,
                                        @Param("tenantId") String tenantId);

    @Modifying
    @Query("DELETE FROM I18nMessage m WHERE m.tenantId = :tenantId")
    void deleteAllByTenantId(@Param("tenantId") String tenantId);
}
