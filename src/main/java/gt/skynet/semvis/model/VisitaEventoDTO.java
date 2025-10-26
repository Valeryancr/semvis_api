package gt.skynet.semvis.model;

import gt.skynet.semvis.entity.VisitaEvento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitaEventoDTO {

    private Long id;
    private Long visitaId;
    private String tipo;
    private OffsetDateTime fecha;
    private Double lat;
    private Double lon;
    private String nota;
    private String creadoPor;
    private OffsetDateTime createdAt;

    public static VisitaEventoDTO fromEntity(VisitaEvento evento) {
        if (evento == null) return null;
        return VisitaEventoDTO.builder()
                .id(evento.getId())
                .visitaId(evento.getVisita() != null ? evento.getVisita().getId() : null)
                .tipo(evento.getTipo().name())
                .fecha(evento.getFecha())
                .lat(evento.getLat())
                .lon(evento.getLon())
                .nota(evento.getNota())
                .creadoPor(evento.getCreatedBy())
                .createdAt(evento.getCreatedAt())
                .build();
    }

    public VisitaEvento toEntity(gt.skynet.semvis.entity.Visita visita) {
        VisitaEvento entity = new VisitaEvento();
        entity.setId(this.id);
        entity.setVisita(visita);
        entity.setLat(this.lat);
        entity.setLon(this.lon);
        entity.setNota(this.nota);
        entity.setFecha(this.fecha != null ? this.fecha : OffsetDateTime.now());
        try {
            entity.setTipo(VisitaEvento.VisitEventType.valueOf(this.tipo.toUpperCase()));
        } catch (Exception ex) {
            entity.setTipo(VisitaEvento.VisitEventType.CHECKIN);
        }
        return entity;
    }
}