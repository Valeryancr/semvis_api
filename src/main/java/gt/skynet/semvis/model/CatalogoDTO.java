package gt.skynet.semvis.model;

import gt.skynet.semvis.entity.Departamento;
import gt.skynet.semvis.entity.Municipio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoDTO {

    private Integer id;
    private String nombre;
    private Integer departamentoId;

    public static CatalogoDTO fromEntity(Departamento d) {
        if (d == null) return null;
        return CatalogoDTO.builder()
                .id(d.getId())
                .nombre(d.getNombre())
                .build();
    }

    public static CatalogoDTO fromEntity(Municipio m) {
        if (m == null) return null;
        return CatalogoDTO.builder()
                .id(m.getId())
                .nombre(m.getNombre())
                .departamentoId(m.getDepartamento() != null ? m.getDepartamento().getId() : null)
                .build();
    }
}