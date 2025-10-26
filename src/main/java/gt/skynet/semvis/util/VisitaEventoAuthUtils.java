package gt.skynet.semvis.util;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.repository.VisitaRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class VisitaEventoAuthUtils {

    @Autowired
    private AuthUtils auth;

    @Autowired
    private VisitaRepo visitaRepo;

    private static final Logger LOG = LoggerFactory.getLogger(VisitaEventoAuthUtils.class);

    /**
     * Verifica si el usuario puede registrar un evento (CHECKIN/CHECKOUT).
     * ADMIN y SUPERVISOR pueden registrar cualquiera.
     * TECNICO solo puede registrar en sus propias visitas.
     * CLIENTE no puede.
     */
    public void verificarPermisoRegistrar(Long visitaId) {
        User user = auth.getAuthenticatedUser();

        if (auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            return;
        }

        if (auth.hasRole("CLIENTE")) {
            LOG.warn("Intento no autorizado de registro de evento por cliente: {}", user.getUsername());
            throw new AccessDeniedException("Los clientes no pueden registrar eventos.");
        }

        if (auth.hasRole("TECNICO")) {
            Visita v = visitaRepo.findById(visitaId)
                    .orElseThrow(() -> new AccessDeniedException("Visita no encontrada."));
            if (v.getTecnico() == null || !v.getTecnico().getId().equals(user.getId())) {
                LOG.warn("Intento no autorizado de registro de evento por tecnico: {}", user.getUsername());
                throw new AccessDeniedException("No puede registrar eventos para visitas de otros técnicos.");
            }
        }
    }

    /**
     * Verifica si el usuario puede ver los eventos de una visita.
     * ADMIN y SUPERVISOR pueden ver todos.
     * TECNICO solo los suyos.
     * CLIENTE solo los suyos.
     */
    public void verificarPermisoVerEventos(Long visitaId) {
        User user = auth.getAuthenticatedUser();

        if (auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            return;
        }

        Visita v = visitaRepo.findById(visitaId)
                .orElseThrow(() -> new AccessDeniedException("Visita no encontrada."));

        if (auth.hasRole("TECNICO")) {
            if (v.getTecnico() == null || !v.getTecnico().getId().equals(user.getId())) {
                LOG.warn("Intento no autorizado de ver eventos de visita por tecnico: {}", user.getUsername());
                throw new AccessDeniedException("No puede ver eventos de visitas de otros técnicos.");
            }
        } else if (auth.hasRole("CLIENTE")) {
            if (v.getCliente() == null || v.getCliente().getUser() == null ||
                    !v.getCliente().getUser().getId().equals(user.getId())) {
                LOG.warn("Intento no autorizado de ver eventos de visita por usuario: {}", user.getUsername());
                throw new AccessDeniedException("No puede ver eventos de otras visitas.");
            }
        } else {
            throw new AccessDeniedException("No tiene permisos para ver eventos.");
        }
    }
}