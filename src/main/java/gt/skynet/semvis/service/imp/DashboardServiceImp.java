package gt.skynet.semvis.service.imp;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.model.DashboardDTO;
import gt.skynet.semvis.repository.DashboardRepo;
import gt.skynet.semvis.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class DashboardServiceImp implements DashboardService {

    @Autowired
    private DashboardRepo dashboardRepo;

    @Override
    public DashboardDTO obtenerResumen(OffsetDateTime inicio, OffsetDateTime fin) {
        if (inicio == null || fin == null) {
            fin = OffsetDateTime.now();
            inicio = fin.minusDays(7);
        }

        long total = dashboardRepo.countTotal(inicio, fin);
        long programadas = dashboardRepo.countByEstado(Visita.VisitState.PROGRAMADA, inicio, fin);
        long enCurso = dashboardRepo.countByEstado(Visita.VisitState.EN_CURSO, inicio, fin);
        long completadas = dashboardRepo.countByEstado(Visita.VisitState.COMPLETADA, inicio, fin);
        long canceladas = dashboardRepo.countByEstado(Visita.VisitState.CANCELADA, inicio, fin);

        return DashboardDTO.builder()
                .totalVisitas(total)
                .programadas(programadas)
                .enCurso(enCurso)
                .completadas(completadas)
                .canceladas(canceladas)
                .build();
    }
}
