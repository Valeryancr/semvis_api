package gt.skynet.semvis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardUsuarioDTO {
    private Long usuarioId;
    private String nombreCompleto;
    private String rol;
    private Long totalVisitas;
    private Long programadas;
    private Long enCurso;
    private Long completadas;
    private Long canceladas;
}
