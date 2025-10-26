package gt.skynet.semvis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class DashboardAuthUtils {

    @Autowired
    private AuthUtils auth;

    private static final Logger LOG = LoggerFactory.getLogger(DashboardAuthUtils.class);

    /**
     * Verifica que el usuario tenga rol ADMIN o SUPERVISOR.
     * Lanza AccessDeniedException si no lo tiene.
     */
    public void verificarAccesoDashboard() {
        if (!auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            LOG.warn("Intento no autorizado de acceso a Dashboard por usuario: {}", auth.getAuthenticatedUser().getUsername());
            throw new AccessDeniedException("Solo administradores o supervisores pueden acceder al panel de control.");
        }

    }
}