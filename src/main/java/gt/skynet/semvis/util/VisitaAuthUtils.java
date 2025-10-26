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
public class VisitaAuthUtils {

    @Autowired
    private AuthUtils auth;

    @Autowired
    private VisitaRepo visitaRepo;

    private static final Logger LOG = LoggerFactory.getLogger(VisitaAuthUtils.class);

    /**
     * Verifica si el usuario puede crear una visita.
     * Solo ADMIN y SUPERVISOR pueden crear nuevas visitas.
     */
    public void verificarPermisoCrear() {
        if (!auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            LOG.warn("Intento no autorizado de crear visita por usuario: {}", auth.getAuthenticatedUser().getUsername());
            throw new AccessDeniedException("Solo administradores o supervisores pueden crear visitas.");
        }
    }

    /**
     * Verifica si el usuario puede ver la visita.
     * ADMIN y SUPERVISOR -> siempre
     * TECNICO -> si la visita le pertenece
     * CLIENTE -> si la visita le pertenece como cliente
     */
    public void verificarPermisoVer(Long visitaId) {
        User actual = auth.getAuthenticatedUser();

        if (auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            return;
        }

        Visita visita = visitaRepo.findById(visitaId)
                .orElseThrow(() -> new AccessDeniedException("Visita no encontrada."));

        if (auth.hasRole("TECNICO")) {
            if (visita.getTecnico() == null || !visita.getTecnico().getId().equals(actual.getId())) {
                LOG.warn("Intento no autorizado de acceso a visita por tecnico: {}", auth.getAuthenticatedUser().getUsername());
                throw new AccessDeniedException("No tiene permiso para ver esta visita.");
            }
        } else if (auth.hasRole("CLIENTE")) {
            if (visita.getCliente() == null || visita.getCliente().getUser() == null ||
                    !visita.getCliente().getUser().getId().equals(actual.getId())) {
                LOG.warn("Intento no autorizado de acceso a visita por cliente: {}", auth.getAuthenticatedUser().getUsername());
                throw new AccessDeniedException("No tiene permiso para ver esta visita.");
            }
        } else {
            LOG.warn("Intento no autorizado de acceso a visita por usuario: {}", auth.getAuthenticatedUser().getUsername());
            throw new AccessDeniedException("No tiene permiso para ver esta visita.");
        }
    }

    /**
     * Verifica si el usuario puede modificar la visita.
     * ADMIN o SUPERVISOR -> sí
     * TECNICO -> solo si la visita le pertenece
     * CLIENTE -> nunca
     */
    public void verificarPermisoModificar(Long visitaId) {
        User actual = auth.getAuthenticatedUser();

        if (auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            return;
        }

        if (auth.hasRole("CLIENTE")) {
            LOG.warn("Intento no autorizado de modificacion de visita por cliente: {}", auth.getAuthenticatedUser().getUsername());
            throw new AccessDeniedException("Los clientes no pueden modificar visitas.");
        }

        if (auth.hasRole("TECNICO")) {
            Visita visita = visitaRepo.findById(visitaId)
                    .orElseThrow(() -> new AccessDeniedException("Visita no encontrada."));
            if (visita.getTecnico() == null || !visita.getTecnico().getId().equals(actual.getId())) {
                LOG.warn("Intento no autorizado de modificacion de visita por tecnico: {}", auth.getAuthenticatedUser().getUsername());
                throw new AccessDeniedException("No puede modificar visitas asignadas a otros técnicos.");
            }
        }
    }

    /**
     * Verifica si el usuario puede cambiar el estado de la visita.
     * ADMIN y SUPERVISOR -> siempre
     * TECNICO -> solo si la visita le pertenece
     * CLIENTE -> nunca
     */
    public void verificarPermisoCambioEstado(Long visitaId) {
        User actual = auth.getAuthenticatedUser();

        if (auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            return;
        }

        if (auth.hasRole("CLIENTE")) {
            LOG.warn("Intento no autorizado de cambio de estado de visita por cliente: {}", auth.getAuthenticatedUser().getUsername());
            throw new AccessDeniedException("Los clientes no pueden cambiar el estado de visitas.");
        }

        if (auth.hasRole("TECNICO")) {
            Visita visita = visitaRepo.findById(visitaId)
                    .orElseThrow(() -> new AccessDeniedException("Visita no encontrada."));
            if (visita.getTecnico() == null || !visita.getTecnico().getId().equals(actual.getId())) {
                LOG.warn("Intento no autorizado de cambio de estado de visita por tecnico: {}", auth.getAuthenticatedUser().getUsername());
                throw new AccessDeniedException("No puede cambiar el estado de visitas asignadas a otros técnicos.");
            }
        }
    }

    /**
     * Verifica si el usuario puede listar visitas.
     * ADMIN y SUPERVISOR -> todas
     * TECNICO -> solo las suyas
     * CLIENTE -> solo las suyas
     */
    public void verificarPermisoListarTodas() {
        if (!auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            LOG.warn("Intento no autorizado de listar todas las visitas por usuario: {}", auth.getAuthenticatedUser().getUsername());
            throw new AccessDeniedException("Solo administradores o supervisores pueden listar todas las visitas.");
        }
    }

    /**
     * Determina si el usuario actual puede listar visitas de un técnico.
     */
    public void verificarPermisoListarPorTecnico(Long tecnicoId) {
        User actual = auth.getAuthenticatedUser();
        if (!auth.hasAnyRole("ADMIN", "SUPERVISOR") &&
                (!auth.hasRole("TECNICO") || !actual.getId().equals(tecnicoId))) {
            LOG.warn("Intento no autorizado de listar visitas por tecnico: {}", auth.getAuthenticatedUser().getUsername());
            throw new AccessDeniedException("No puede ver las visitas de otros técnicos.");
        }
    }

    /**
     * Determina si el usuario actual puede listar visitas de un supervisor.
     */
    public void verificarPermisoListarPorSupervisor(Long supervisorId) {
        User actual = auth.getAuthenticatedUser();

        if (!auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            LOG.warn("Intento no autorizado de listar visitas por usuario: {}", auth.getAuthenticatedUser().getUsername());
            throw new AccessDeniedException("Solo administradores o supervisores pueden ver visitas por supervisor.");
        }

        if (auth.hasRole("SUPERVISOR") && !actual.getId().equals(supervisorId)) {
            LOG.warn("Intento no autorizado de listar visitas por supervisor: {}", auth.getAuthenticatedUser().getUsername());
            throw new AccessDeniedException("No puede ver visitas de otros supervisores.");
        }
    }

    /**
     * Determina si el usuario actual puede listar visitas de un cliente.
     */
    public void verificarPermisoListarPorCliente(Long clienteId) {
        User actual = auth.getAuthenticatedUser();
        if (!auth.hasAnyRole("ADMIN", "SUPERVISOR") &&
                (!auth.hasRole("CLIENTE") || !actual.getId().equals(clienteId))) {
            LOG.warn("Intento no autorizado de listar visitas por cliente: {}", auth.getAuthenticatedUser().getUsername());
            throw new AccessDeniedException("No puede ver las visitas de otros clientes.");
        }
    }

}