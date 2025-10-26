package gt.skynet.semvis.controller;

import gt.skynet.semvis.model.DashboardDTO;
import gt.skynet.semvis.service.DashboardService;
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
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DashboardAuthUtils dashboardAuth;

    @GetMapping("/resumen")
    public ResponseEntity<Response> obtenerResumen(@RequestParam(name = "inicio", required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
                                                   @RequestParam(name = "fin", required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fin) {

        Response res = new Response();
        try {
            dashboardAuth.verificarAccesoDashboard();
            DashboardDTO dto = dashboardService.obtenerResumen(inicio, fin);
            res.setData(dto);
            res.setCode(200);
            res.setMsg("Resumen obtenido correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener resumen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }
}