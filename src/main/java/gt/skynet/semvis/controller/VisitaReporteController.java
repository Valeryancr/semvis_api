package gt.skynet.semvis.controller;

import gt.skynet.semvis.model.VisitaReporteDTO;
import gt.skynet.semvis.service.VisitaReporteService;
import gt.skynet.semvis.util.Response;
import gt.skynet.semvis.util.VisitaReporteAuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/visitas/reportes")
public class VisitaReporteController {

    @Autowired
    private VisitaReporteService reporteService;

    @Autowired
    private VisitaReporteAuthUtils reporteAuth;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> crearReporte(@RequestParam Long visitaId,
                                                 @RequestPart(required = false) MultipartFile pdfFile,
                                                 @RequestParam(required = false) String storageUrl,
                                                 @RequestParam(required = false) String name) {

        Response res = new Response();
        try {
            reporteAuth.verificarPermisoGenerar(visitaId);
            res.setData(reporteService.crearReporte(visitaId, pdfFile, storageUrl, name));
            res.setMsg("Reporte creado correctamente.");
            res.setCode(200);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al crear reporte: " + e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/visita/{id}")
    public ResponseEntity<Response> listarPorVisita(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            reporteAuth.verificarPermisoObtener(id);
            List<VisitaReporteDTO> lista = reporteService.listarPorVisita(id);
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Reportes obtenidos correctamente (" + lista.size() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar reportes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PutMapping("/{id}/enviar")
    public ResponseEntity<Response> marcarComoEnviado(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            res.setData(reporteService.marcarComoEnviado(id));
            res.setCode(200);
            res.setMsg("Reporte marcado como enviado.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al marcar reporte como enviado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable("id") Long id) {
        reporteAuth.verificarPermisoVer(id);

        byte[] pdf = reporteService.obtenerPdf(id);
        if (pdf == null || pdf.length == 0)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/todos")
    public ResponseEntity<Response> verTodos(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String q) {

        Response res = new Response();
        try {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<VisitaReporteDTO> reportes = reporteService.verTodosReportes(q, pageable);

            res.setData(reportes);
            res.setCode(200);
            res.setMsg("Listado de reportes obtenido correctamente (" + reportes.getTotalElements() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener reportes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/supervisor/{id}")
    public ResponseEntity<Response> listarPorSupervisor(@PathVariable("id") Long id,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String q) {

        Response res = new Response();
        try {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<VisitaReporteDTO> reportes = reporteService.listarPorSupervisor(id, q, pageable);

            res.setData(reportes);
            res.setCode(200);
            res.setMsg("Reportes del supervisor " + id + " obtenidos correctamente (" + reportes.getTotalElements() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener reportes por supervisor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }
}
