package gt.skynet.semvis.util;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.entity.VisitaReporte;
import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.repository.VisitaRepo;
import gt.skynet.semvis.repository.VisitaReporteRepo;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class VisitaReporteAuthUtils {

    @Autowired
    private AuthUtils auth;

    @Autowired
    private VisitaRepo visitaRepo;

    @Autowired
    private VisitaReporteRepo reportes;

    private static final Logger LOG = LoggerFactory.getLogger(VisitaReporteAuthUtils.class);

    /**
     * Permite generar un reporte solo a ADMIN, SUPERVISOR o al técnico asignado.
     * Clientes no pueden generar.
     */
    public void verificarPermisoGenerar(Long visitaId) {
        User user = auth.getAuthenticatedUser();

        if (auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            return;
        }

        if (auth.hasRole("CLIENTE")) {
            LOG.warn("Intento no autorizado de generar reporte por cliente: {}", user.getUsername());
            throw new AccessDeniedException("Los clientes no pueden generar reportes de visitas.");
        }

        if (auth.hasRole("TECNICO")) {
            Visita v = visitaRepo.findById(visitaId)
                    .orElseThrow(() -> new AccessDeniedException("Visita no encontrada."));
            if (v.getTecnico() == null || !v.getTecnico().getId().equals(user.getId())) {
                LOG.warn("Intento no autorizado de generar reporte por tecnico: {}", user.getUsername());
                throw new AccessDeniedException("No puede generar reportes de visitas asignadas a otros técnicos.");
            }
        } else {
            LOG.warn("Intento no autorizado de generar reporte por usuario: {}", user.getUsername());
            throw new AccessDeniedException("No tiene permisos para generar reportes.");
        }
    }

    /**
     * Permite obtener PDFs de una visita.
     * ADMIN y SUPERVISOR pueden ver todo.
     * TECNICO: solo si le pertenece.
     * CLIENTE: solo si es su visita.
     */
    public void verificarPermisoObtener(Long visitaId) {
        User user = auth.getAuthenticatedUser();

        if (auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            return;
        }

        Visita v = visitaRepo.findById(visitaId)
                .orElseThrow(() -> new AccessDeniedException("Visita no encontrada."));

        if (auth.hasRole("TECNICO")) {
            if (v.getTecnico() == null || !v.getTecnico().getId().equals(user.getId())) {
                LOG.warn("Intento no autorizado de obtener reportes por tecnico {}", user.getUsername());
                throw new AccessDeniedException("No tiene permiso para ver reportes de otras visitas.");
            }
        } else if (auth.hasRole("CLIENTE")) {
            if (v.getCliente() == null || v.getCliente().getUser() == null ||
                    !v.getCliente().getUser().getId().equals(user.getId())) {
                LOG.warn("Intento no autorizado de obtener reportes por cliente {}", user.getUsername());
                throw new AccessDeniedException("No tiene permiso para ver este reporte.");
            }
        } else {
            LOG.warn("Intento no autorizado de obtener reportes por usuario {}", user.getUsername());
            throw new AccessDeniedException("No tiene permisos para ver reportes.");
        }
    }

    /**
     * Permite visualizar PDFs por ID.
     * ADMIN y SUPERVISOR pueden ver todo.
     * TECNICO: solo si le pertenece.
     * CLIENTE: solo si es su visita.
     */
    public void verificarPermisoVer(Long reportId) {
        User user = auth.getAuthenticatedUser();

        if (auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
            return;
        }

        VisitaReporte reporte = reportes.findById(reportId).orElseThrow(() -> new EntityNotFoundException("No se ha encontrado el reporte referenciaddo"));

        Visita v = visitaRepo.findById(reporte.getVisita().getId())
                .orElseThrow(() -> new AccessDeniedException("Visita no encontrada."));

        if (auth.hasRole("TECNICO")) {
            if (v.getTecnico() == null || !v.getTecnico().getId().equals(user.getId())) {
                LOG.warn("Intento no autorizado de visualizar reporte por tecnico {}", user.getUsername());
                throw new AccessDeniedException("No tiene permiso para ver reportes de otras visitas.");
            }
        } else if (auth.hasRole("CLIENTE")) {
            if (v.getCliente() == null || v.getCliente().getUser() == null ||
                    !v.getCliente().getUser().getId().equals(user.getId())) {
                LOG.warn("Intento no autorizado de visualizar reporte por cliente {}", user.getUsername());
                throw new AccessDeniedException("No tiene permiso para ver este reporte.");
            }
        } else {
            LOG.warn("Intento no autorizado de visualizar reporte por usuario {}", user.getUsername());
            throw new AccessDeniedException("No tiene permisos para ver reportes.");
        }
    }
}