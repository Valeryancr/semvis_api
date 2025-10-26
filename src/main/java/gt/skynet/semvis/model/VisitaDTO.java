package gt.skynet.semvis.model;

import gt.skynet.semvis.entity.Cliente;
import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitaDTO {

    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private String direccionFMT;
    private Long supervisorId;
    private String supervisorNombre;
    private Long tecnicoId;
    private String tecnicoNombre;
    private OffsetDateTime fechaProgramada;
    private String estado;
    private String observacionesPlan;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private List<VisitaEventoDTO> eventos;

    public static VisitaDTO fromEntity(Visita visita) {
        if (visita == null) return null;

        return VisitaDTO.builder()
                .id(visita.getId())
                .clienteId(visita.getCliente() != null ? visita.getCliente().getId() : null)
                .clienteNombre(visita.getCliente() != null ? visita.getCliente().getNombre() : null)
                .direccionFMT(visita.getCliente() != null ? visita.getCliente().getDireccion().getDireccionFmt() : null)
                .supervisorId(visita.getSupervisor() != null ? visita.getSupervisor().getId() : null)
                .supervisorNombre(visita.getSupervisor() != null ? visita.getSupervisor().getNombreCompleto() : null)
                .tecnicoId(visita.getTecnico() != null ? visita.getTecnico().getId() : null)
                .tecnicoNombre(visita.getTecnico() != null ? visita.getTecnico().getNombreCompleto() : null)
                .fechaProgramada(visita.getFechaProgramada())
                .estado(visita.getEstado().name())
                .observacionesPlan(visita.getObservacionesPlan())
                .completedAt(visita.getCompletedAt())
                .createdAt(visita.getCreatedAt())
                .updatedAt(visita.getUpdatedAt())
                .build();
    }

    public static VisitaDTO fromEntityWithEvents(Visita visita) {
        if (visita == null) return null;

        return VisitaDTO.builder()
                .id(visita.getId())
                .clienteId(visita.getCliente() != null ? visita.getCliente().getId() : null)
                .clienteNombre(visita.getCliente() != null ? visita.getCliente().getNombre() : null)
                .direccionFMT(visita.getCliente() != null ? visita.getCliente().getDireccion().getDireccionFmt() : null)
                .supervisorId(visita.getSupervisor() != null ? visita.getSupervisor().getId() : null)
                .supervisorNombre(visita.getSupervisor() != null ? visita.getSupervisor().getNombreCompleto() : null)
                .tecnicoId(visita.getTecnico() != null ? visita.getTecnico().getId() : null)
                .tecnicoNombre(visita.getTecnico() != null ? visita.getTecnico().getNombreCompleto() : null)
                .fechaProgramada(visita.getFechaProgramada())
                .estado(visita.getEstado().name())
                .observacionesPlan(visita.getObservacionesPlan())
                .completedAt(visita.getCompletedAt())
                .createdAt(visita.getCreatedAt())
                .updatedAt(visita.getUpdatedAt())
                .eventos(visita.getEventos() != null
                        ? visita.getEventos().stream()
                        .map(VisitaEventoDTO::fromEntity)
                        .toList()
                        : List.of())
                .build();
    }

    public Visita toEntity(Cliente cliente, User supervisor, User tecnico) {
        Visita visita = new Visita();
        visita.setId(this.id);
        visita.setCliente(cliente);
        visita.setSupervisor(supervisor);
        visita.setTecnico(tecnico);
        visita.setFechaProgramada(this.fechaProgramada);
        visita.setObservacionesPlan(this.observacionesPlan);
        if (this.estado != null) {
            try {
                visita.setEstado(Visita.VisitState.valueOf(this.estado.toUpperCase()));
            } catch (IllegalArgumentException e) {
                visita.setEstado(Visita.VisitState.PROGRAMADA);
            }
        } else {
            visita.setEstado(Visita.VisitState.PROGRAMADA);
        }
        visita.setCompletedAt(this.completedAt);
        return visita;
    }

    public static VisitaDTO fromEntityForCliente(Visita visita) {
        if (visita == null) return null;

        return VisitaDTO.builder()
                .id(visita.getId())
                .clienteId(visita.getCliente() != null ? visita.getCliente().getId() : null)
                .clienteNombre(visita.getCliente() != null ? visita.getCliente().getNombre() : null)
                .tecnicoId(visita.getTecnico() != null ? visita.getTecnico().getId() : null)
                .tecnicoNombre(visita.getTecnico() != null ? visita.getTecnico().getNombreCompleto() : null)
                .fechaProgramada(visita.getFechaProgramada())
                .estado(visita.getEstado().name())
                .observacionesPlan(visita.getObservacionesPlan())
                .completedAt(visita.getCompletedAt())
                .createdAt(visita.getCreatedAt())
                .updatedAt(visita.getUpdatedAt())
                .build();
    }
}