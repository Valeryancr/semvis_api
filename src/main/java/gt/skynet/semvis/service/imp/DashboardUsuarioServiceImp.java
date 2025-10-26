package gt.skynet.semvis.service.imp;

import gt.skynet.semvis.model.DashboardUsuarioDTO;
import gt.skynet.semvis.repository.DashboardUsuarioRepo;
import gt.skynet.semvis.service.DashboardUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardUsuarioServiceImp implements DashboardUsuarioService {

    @Autowired
    private DashboardUsuarioRepo repo;

    private DashboardUsuarioDTO mapRow(Object[] row) {
        return DashboardUsuarioDTO.builder()
                .usuarioId(((Number) row[0]).longValue())
                .nombreCompleto((String) row[1])
                .rol((String) row[2])
                .totalVisitas(((Number) row[3]).longValue())
                .programadas(((Number) row[4]).longValue())
                .enCurso(((Number) row[5]).longValue())
                .completadas(((Number) row[6]).longValue())
                .canceladas(((Number) row[7]).longValue())
                .build();
    }

    @Override
    public List<DashboardUsuarioDTO> obtenerResumenPorTecnico(OffsetDateTime inicio, OffsetDateTime fin) {
        if (inicio == null || fin == null) {
            fin = OffsetDateTime.now();
            inicio = fin.minusDays(7);
        }
        return repo.resumenPorTecnico(inicio, fin)
                .stream()
                .map(this::mapRow)
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardUsuarioDTO> obtenerResumenPorSupervisor(OffsetDateTime inicio, OffsetDateTime fin) {
        if (inicio == null || fin == null) {
            fin = OffsetDateTime.now();
            inicio = fin.minusDays(7);
        }
        return repo.resumenPorSupervisor(inicio, fin)
                .stream()
                .map(this::mapRow)
                .collect(Collectors.toList());
    }
}
