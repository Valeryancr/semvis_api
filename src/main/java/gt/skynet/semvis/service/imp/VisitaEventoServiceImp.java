package gt.skynet.semvis.service.imp;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.entity.VisitaEvento;
import gt.skynet.semvis.model.VisitaEventoDTO;
import gt.skynet.semvis.repository.VisitaEventoRepo;
import gt.skynet.semvis.repository.VisitaRepo;
import gt.skynet.semvis.service.ReporteService;
import gt.skynet.semvis.service.VisitaEventoService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisitaEventoServiceImp implements VisitaEventoService {

    @Autowired
    private VisitaEventoRepo eventos;

    @Autowired
    private VisitaRepo visitas;

    @Autowired
    private ReporteService reporteService;

    private static final Logger LOG = LoggerFactory.getLogger(VisitaEventoServiceImp.class);

    @Transactional
    @Override
    public VisitaEventoDTO registrarEvento(VisitaEventoDTO dto) {
        Visita visita = visitas.findById(dto.getVisitaId())
                .orElseThrow(() -> new RuntimeException("Visita no encontrada con ID " + dto.getVisitaId()));

        List<VisitaEvento> existentes = eventos.findByVisitaAndTipo(visita,
                VisitaEvento.VisitEventType.valueOf(dto.getTipo().toUpperCase()));
        if (!existentes.isEmpty())
            throw new RuntimeException("Ya existe un evento de tipo " + dto.getTipo() + " para esta visita.");

        if(dto.getTipo().equals("CHECKIN"))
            visita.setEstado(Visita.VisitState.EN_CURSO);


        VisitaEvento nuevo = dto.toEntity(visita);
        eventos.save(nuevo);

        if (nuevo.getTipo() == VisitaEvento.VisitEventType.CHECKOUT) {
            visita.setEstado(Visita.VisitState.COMPLETADA);
            visita.setCompletedAt(OffsetDateTime.now());

            visitas.save(visita);
            try {
                reporteService.generarReporte(visita);
                LOG.info("Reporte generado y correo enviado para visita ID={}", visita.getId());
            } catch (Exception e) {
                LOG.error("Error al generar reporte o enviar correo para visita ID={}: {}", visita.getId(), e.getMessage(), e);
            }
        }

        return VisitaEventoDTO.fromEntity(nuevo);
    }

    @Override
    public List<VisitaEventoDTO> listarEventosPorVisita(Long visitaId) {
        return eventos.findAllByVisitaId(visitaId)
                .stream()
                .map(VisitaEventoDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
