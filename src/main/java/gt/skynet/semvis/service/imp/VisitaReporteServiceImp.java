package gt.skynet.semvis.service.imp;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.entity.VisitaReporte;
import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.model.VisitaReporteDTO;
import gt.skynet.semvis.repository.VisitaRepo;
import gt.skynet.semvis.repository.VisitaReporteRepo;
import gt.skynet.semvis.service.VisitaReporteService;
import gt.skynet.semvis.util.AuthUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisitaReporteServiceImp implements VisitaReporteService {

    @Autowired
    private VisitaReporteRepo reportes;

    @Autowired
    private VisitaRepo visitas;

    @Autowired
    private AuthUtils auth;

    @Transactional
    @Override
    public VisitaReporteDTO crearReporte(Long visitaId, MultipartFile pdfFile, String storageUrl, String nombre) throws IOException {
        Visita visita = visitas.findById(visitaId)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada con ID " + visitaId));

        VisitaReporte reporte = new VisitaReporte();
        reporte.setVisita(visita);
        reporte.setStorageUrl(storageUrl);

        if (pdfFile != null && !pdfFile.isEmpty()) {
            reporte.setPdfBlob(pdfFile.getBytes());

            String original = pdfFile.getOriginalFilename();
            if (original != null && !original.isBlank()) {
                if (!original.toLowerCase().endsWith(".pdf")) {
                    original += ".pdf";
                }
                reporte.setNombreArchivo(original);
            } else if (nombre != null && !nombre.isBlank()) {
                reporte.setNombreArchivo(nombre + ".pdf");
            } else {
                reporte.setNombreArchivo("reporte_visita_" + visitaId + ".pdf");
            }
        } else if (nombre != null && !nombre.isBlank()) {
            reporte.setNombreArchivo(nombre + ".pdf");
        }

        User user = auth.getAuthenticatedUser();
        reporte.setUsuarioCarga(user);

        reporte.setEnviadoEmail(false);
        reportes.save(reporte);

        return VisitaReporteDTO.fromEntity(reporte);
    }

    @Override
    public List<VisitaReporteDTO> listarPorVisita(Long visitaId) {
        return reportes.findByVisitaId(visitaId)
                .stream()
                .map(VisitaReporteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public VisitaReporteDTO marcarComoEnviado(Long id) {
        VisitaReporte reporte = reportes.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con ID " + id));
        reporte.setEnviadoEmail(true);
        reporte.setSentAt(OffsetDateTime.now());
        reportes.save(reporte);
        return VisitaReporteDTO.fromEntity(reporte);
    }

    @Override
    public byte[] obtenerPdf(Long id) {
        VisitaReporte reporte = reportes.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con ID " + id));
        return reporte.getPdfBlob();
    }

    @Override
    public Page<VisitaReporteDTO> verTodosReportes(String q, Pageable pageable) {
        Page<VisitaReporte> page = reportes.buscarTodos(q, pageable);
        return page.map(VisitaReporteDTO::fromEntity);
    }

    @Override
    public Page<VisitaReporteDTO> listarPorSupervisor(Long supervisorId, String q, Pageable pageable) {
        Page<VisitaReporte> page = reportes.buscarPorSupervisor(supervisorId, q, pageable);
        return page.map(VisitaReporteDTO::fromEntity);
    }
}
