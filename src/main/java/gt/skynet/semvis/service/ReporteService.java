package gt.skynet.semvis.service;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.entity.VisitaReporte;
import jakarta.transaction.Transactional;

public interface ReporteService {
    @Transactional
    VisitaReporte generarReporte(Visita visita);
}
