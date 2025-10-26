package gt.skynet.semvis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCompletoDTO {
    private DashboardDTO resumenGeneral;
    private List<DashboardUsuarioDTO> resumenTecnicos;
    private List<DashboardUsuarioDTO> resumenSupervisores;
    private List<TendenciaDTO> tendenciaGeneral;
    private List<TendenciaDTO> tendenciaPorEstado;
}