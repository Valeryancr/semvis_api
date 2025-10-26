package gt.skynet.semvis.service;

import gt.skynet.semvis.model.DashboardUsuarioDTO;

import java.time.OffsetDateTime;
import java.util.List;

public interface DashboardUsuarioService {
    List<DashboardUsuarioDTO> obtenerResumenPorTecnico(OffsetDateTime inicio, OffsetDateTime fin);

    List<DashboardUsuarioDTO> obtenerResumenPorSupervisor(OffsetDateTime inicio, OffsetDateTime fin);
}
