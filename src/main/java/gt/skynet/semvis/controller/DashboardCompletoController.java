package gt.skynet.semvis.controller;

import gt.skynet.semvis.model.DashboardCompletoDTO;
import gt.skynet.semvis.service.DashboardCompletoService;
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

@CrossOrigin
@RestController
@RequestMapping("/dashboard")
public class DashboardCompletoController {

    @Autowired
    private DashboardCompletoService dashboardCompletoService;

    @Autowired
    private DashboardAuthUtils dashboardAuth;

    @GetMapping("/completo")
    public ResponseEntity<Response> obtenerDashboardCompleto(@RequestParam(name = "inicio", required = false)
                                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
                                                             @RequestParam(name = "fin", required = false)
                                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fin) {

        Response res = new Response();
        try {
            dashboardAuth.verificarAccesoDashboard();
            DashboardCompletoDTO dto = dashboardCompletoService.obtenerDashboardCompleto(inicio, fin);
            res.setData(dto);
            res.setCode(200);
            res.setMsg("Dashboard completo obtenido correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener dashboard completo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }
}