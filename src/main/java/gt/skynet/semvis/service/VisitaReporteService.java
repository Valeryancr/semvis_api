package gt.skynet.semvis.service;

import gt.skynet.semvis.model.VisitaReporteDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VisitaReporteService {

    @Transactional
    VisitaReporteDTO crearReporte(Long visitaId, MultipartFile pdfFile, String storageUrl, String nombre) throws IOException;

    List<VisitaReporteDTO> listarPorVisita(Long visitaId);

    @Transactional
    VisitaReporteDTO marcarComoEnviado(Long id);

    byte[] obtenerPdf(Long id);

    Page<VisitaReporteDTO> verTodosReportes(String q, Pageable pageable);

    Page<VisitaReporteDTO> listarPorSupervisor(Long supervisorId, String q, Pageable pageable);
}
