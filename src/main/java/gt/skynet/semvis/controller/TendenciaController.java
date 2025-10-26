package gt.skynet.semvis.controller;

import gt.skynet.semvis.model.TendenciaDTO;
import gt.skynet.semvis.service.TendenciaService;
import gt.skynet.semvis.util.DashboardAuthUtils;
import gt.skynet.semvis.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/dashboard/tendencias")
public class TendenciaController {

    @Autowired
    private TendenciaService service;

    @Autowired
    private DashboardAuthUtils dashboardAuth;

    @GetMapping("/general")
    public ResponseEntity<Response> tendenciaGeneral(@RequestParam(name = "inicio", required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
                                                     @RequestParam(name = "fin", required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fin) {

        Response res = new Response();
        dashboardAuth.verificarAccesoDashboard();
        List<TendenciaDTO> lista = service.obtenerTendenciaGeneral(inicio, fin);
        res.setData(lista);
        res.setCode(200);
        res.setMsg("Tendencia general obtenida correctamente (" + lista.size() + " registros).");
            return ResponseEntity.ok(res);
    }

    @GetMapping("/estado")
    public ResponseEntity<Response> tendenciaPorEstado(@RequestParam(name = "inicio", required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
                                                       @RequestParam(name = "fin", required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fin) {

        Response res = new Response();
        dashboardAuth.verificarAccesoDashboard();
        List<TendenciaDTO> lista = service.obtenerTendenciaPorEstado(inicio, fin);
        res.setData(lista);
        res.setCode(200);
        res.setMsg("Tendencia por estado obtenida correctamente (" + lista.size() + " registros).");
        return ResponseEntity.ok(res);
    }
}