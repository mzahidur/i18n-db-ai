package io.github.mzahidur.i18n.domain.port;

/**
 * Domain port for resolving the current tenant identifier.
 *
 * <p>Host applications provide their own implementation — typically reading
 * from a {@code ThreadLocal}, a Spring Security principal, a request header,
 * or a JWT claim.  The library never dictates how tenancy is established,
 * only that a string identifier can be obtained at resolution time.</p>
 *
 * <p>Activated when {@code i18n.db.tenant.enabled=true}.  The bean name of
 * the implementing class is supplied via {@code i18n.db.tenant.resolver}.</p>
 *
 * <p>Contract</p>
 * <ul>
 *   <li>Must never throw — return {@code null} or blank when no tenant context
 *       is available; see io.github.mzahidur.i18n.application.service.TenantAwareCacheKeyResolver
 *       falls back to {@code "_global_"} in that case.</li>
 *   <li>Must be safe for concurrent use (typically backed by a {@code ThreadLocal}).</li>
 * </ul>
 *
 * <h3>Example implementation</h3>
 * <pre>{@code
 * @Component("myTenantResolver")
 * public class JwtTenantIdResolver implements TenantIdResolver {
 *     @Override
 *     public String currentTenantId() {
 *         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *         if (auth instanceof JwtAuthenticationToken jwt) {
 *             return jwt.getToken().getClaimAsString("tenantId");
 *         }
 *         return null;
 *     }
 * }
 * }</pre>
 *
 * <p>Then in {@code application.yml}:</p>
 * <pre>
 * i18n:
 *   db:
 *     tenant:
 *       enabled: true
 *       resolver: myTenantResolver
 * </pre>
 */
public interface TenantIdResolver {

    /**
     * Return the current tenant identifier.
     *
     * @return tenant ID string, or {@code null} / blank if no tenant context is active
     */
    String currentTenantId();
}