package gt.skynet.semvis.controller;

import gt.skynet.semvis.entity.VisitaReporte;
import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.repository.VisitaReporteRepo;
import gt.skynet.semvis.security.JwtService;
import gt.skynet.semvis.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private VisitaReporteRepo repo;

    @Autowired
    private JwtService jwt;

    @Autowired
    private AuthUtils auth;

    @GetMapping("/open/secure")
    public ResponseEntity<byte[]> descargarReporteSeguro(@RequestParam("token") String token) {
        try {
            String username = jwt.getSubject(token);
            Map<String, Object> claims = jwt.getClaims(token);

            if (!"pdf".equals(claims.get("type"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Long reporteId = Long.valueOf(claims.get("rid").toString());
            VisitaReporte reporte = repo.findById(reporteId).orElse(null);

            if (reporte == null || reporte.getPdfBlob() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (auth.hasRole("CLIENTE")) {
                User actual = auth.getAuthenticatedUser();
                if (reporte.getVisita() == null ||
                        reporte.getVisita().getCliente() == null ||
                        reporte.getVisita().getCliente().getUser() == null ||
                        !reporte.getVisita().getCliente().getUser().getId().equals(actual.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            String filename = "reporte_visita_" + reporteId + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(reporte.getPdfBlob());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
