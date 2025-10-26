package gt.skynet.semvis.model;

import gt.skynet.semvis.entity.Direccion;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionDTO {

    private Long id;

    @NotNull
    private Integer departamentoId;

    @NotNull
    private Integer municipioId;

    private Integer zona;
    private String linea1;
    private String linea2;
    private String nivel;
    private String piso;
    private String referencia;
    private String codigoPostal;
    private Double lat;
    private Double lon;
    private String direccionFmt;

    public static DireccionDTO fromEntity(Direccion direccion) {
        if (direccion == null) return null;
        return DireccionDTO.builder()
                .id(direccion.getId())
                .departamentoId(direccion.getDepartamento() != null ? direccion.getDepartamento().getId() : null)
                .municipioId(direccion.getMunicipio() != null ? direccion.getMunicipio().getId() : null)
                .zona(direccion.getZona() != null ? direccion.getZona().intValue() : null)
                .linea1(direccion.getLinea1())
                .linea2(direccion.getLinea2())
                .nivel(direccion.getNivel())
                .piso(direccion.getPiso())
                .referencia(direccion.getReferencia())
                .codigoPostal(direccion.getCodigoPostal())
                .lat(direccion.getLat())
                .lon(direccion.getLon())
                .direccionFmt(direccion.getDireccionFmt())
                .build();
    }

    public Direccion toEntity() {
        Direccion direccion = new Direccion();
        direccion.setId(this.id);
        direccion.setLinea1(this.linea1);
        direccion.setLinea2(this.linea2);
        direccion.setNivel(this.nivel);
        direccion.setPiso(this.piso);
        direccion.setReferencia(this.referencia);
        direccion.setCodigoPostal(this.codigoPostal);
        direccion.setLat(this.lat);
        direccion.setLon(this.lon);
        direccion.setDireccionFmt(this.direccionFmt);
        if (this.zona != null) {
            direccion.setZona(this.zona.shortValue());
        }
        return direccion;
    }
}