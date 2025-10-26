package gt.skynet.semvis.service;

import gt.skynet.semvis.model.VisitaEventoDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface VisitaEventoService {
    @Transactional
    VisitaEventoDTO registrarEvento(VisitaEventoDTO dto);

    List<VisitaEventoDTO> listarEventosPorVisita(Long visitaId);
}
