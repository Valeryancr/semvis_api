package gt.skynet.semvis.service.imp;

import gt.skynet.semvis.model.DashboardCompletoDTO;
import gt.skynet.semvis.model.DashboardDTO;
import gt.skynet.semvis.model.DashboardUsuarioDTO;
import gt.skynet.semvis.model.TendenciaDTO;
import gt.skynet.semvis.service.DashboardCompletoService;
import gt.skynet.semvis.service.DashboardService;
import gt.skynet.semvis.service.DashboardUsuarioService;
import gt.skynet.semvis.service.TendenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DashboardCompletoServiceImp implements DashboardCompletoService {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DashboardUsuarioService dashboardUsuarioService;

    @Autowired
    private TendenciaService tendenciaService;

    @Override
    public DashboardCompletoDTO obtenerDashboardCompleto(OffsetDateTime inicio, OffsetDateTime fin) {
        if (inicio == null || fin == null) {
            fin = OffsetDateTime.now();
            inicio = fin.minusDays(7);
        }

        DashboardDTO resumen = dashboardService.obtenerResumen(inicio, fin);
        List<DashboardUsuarioDTO> tecnicos = dashboardUsuarioService.obtenerResumenPorTecnico(inicio, fin);
        List<DashboardUsuarioDTO> supervisores = dashboardUsuarioService.obtenerResumenPorSupervisor(inicio, fin);
        List<TendenciaDTO> tendencia = tendenciaService.obtenerTendenciaGeneral(inicio, fin);
        List<TendenciaDTO> tendenciaEstado = tendenciaService.obtenerTendenciaPorEstado(inicio, fin);

        return DashboardCompletoDTO.builder()
                .resumenGeneral(resumen)
                .resumenTecnicos(tecnicos)
                .resumenSupervisores(supervisores)
                .tendenciaGeneral(tendencia)
                .tendenciaPorEstado(tendenciaEstado)
                .build();
    }
}