package gt.skynet.semvis.model;

import gt.skynet.semvis.entity.VisitaReporte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitaReporteDTO {
    private Long id;
    private Long visitaId;
    private String storageUrl;
    private Boolean enviadoEmail;
    private OffsetDateTime sentAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String nombreArchivo;
    private String usuarioCarga;
    private Long tecnicoId;
    private Long supervisorId;


    public static VisitaReporteDTO fromEntity(VisitaReporte reporte) {
        if (reporte == null) return null;
        return VisitaReporteDTO.builder()
                .id(reporte.getId())
                .visitaId(reporte.getVisita() != null ? reporte.getVisita().getId() : null)
                .storageUrl(reporte.getStorageUrl())
                .enviadoEmail(reporte.getEnviadoEmail())
                .sentAt(reporte.getSentAt())
                .createdAt(reporte.getCreatedAt())
                .updatedAt(reporte.getUpdatedAt())
                .nombreArchivo(reporte.getNombreArchivo())
                .usuarioCarga(reporte.getUsuarioCarga() != null ?
                        reporte.getUsuarioCarga().getPrimerNombre() + " " + reporte.getUsuarioCarga().getApellido1()
                        : reporte.getCreatedBy())
                .tecnicoId(reporte.getVisita() != null && reporte.getVisita().getTecnico() != null
                        ? reporte.getVisita().getTecnico().getId()
                        : null)
                .supervisorId(reporte.getVisita() != null && reporte.getVisita().getSupervisor() != null
                        ? reporte.getVisita().getSupervisor().getId()
                        : null)
                .build();
    }
}
