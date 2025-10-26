package gt.skynet.semvis.controller;

import gt.skynet.semvis.model.DashboardUsuarioDTO;
import gt.skynet.semvis.service.DashboardUsuarioService;
import gt.skynet.semvis.util.DashboardAuthUtils;
import gt.skynet.semvis.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/dashboard/usuarios")
public class DashboardUsuarioController {

    @Autowired
    private DashboardUsuarioService service;

    @Autowired
    private DashboardAuthUtils dashboardAuth;

    @GetMapping("/tecnicos")
    public ResponseEntity<Response> resumenPorTecnico(@RequestParam(name = "inicio", required = false)
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
                                                      @RequestParam(name = "fin", required = false)
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fin) {

        Response res = new Response();
        try {
            dashboardAuth.verificarAccesoDashboard();
            List<DashboardUsuarioDTO> lista = service.obtenerResumenPorTecnico(inicio, fin);
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Resumen por técnico obtenido correctamente (" + lista.size() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener resumen por técnico: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/supervisores")
    public ResponseEntity<Response> resumenPorSupervisor(@RequestParam(name = "inicio", required = false)
                                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
                                                         @RequestParam(name = "fin", required = false)
                                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fin) {

        Response res = new Response();
        try {
            dashboardAuth.verificarAccesoDashboard();
            List<DashboardUsuarioDTO> lista = service.obtenerResumenPorSupervisor(inicio, fin);
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Resumen por supervisor obtenido correctamente (" + lista.size() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener resumen por supervisor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }
}
