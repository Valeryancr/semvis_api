package gt.skynet.semvis.controller;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.model.VisitaDTO;
import gt.skynet.semvis.service.ClienteService;
import gt.skynet.semvis.service.VisitaService;
import gt.skynet.semvis.util.Response;
import gt.skynet.semvis.util.VisitaAuthUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/visitas")
public class VisitaController {

    @Autowired
    private VisitaService visitaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private VisitaAuthUtils visitaAuth;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> crearVisita(@Valid @RequestBody VisitaDTO dto) {
        Response res = new Response();
        visitaAuth.verificarPermisoCrear();
        res.setData(visitaService.createVisita(dto));
        res.setMsg("Visita creada correctamente.");
        res.setCode(200);
        return ResponseEntity.ok(res);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> actualizarVisita(@PathVariable("id") Long id, @Valid @RequestBody VisitaDTO dto) {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoModificar(id);
            res.setData(visitaService.updateVisita(id, dto));
            res.setMsg("Visita actualizada correctamente.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al actualizar visita: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Response> cambiarEstado(@PathVariable("id") Long id,
                                                  @RequestParam("nuevoEstado") Visita.VisitState nuevoEstado) {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoCambioEstado(id);
            res.setData(visitaService.cambiarEstado(id, nuevoEstado));
            res.setCode(200);
            res.setMsg("Estado actualizado correctamente a " + nuevoEstado + ".");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al cambiar estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping
    public ResponseEntity<Response> listarTodas() {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoListarTodas();
            List<VisitaDTO> lista = visitaService.listarTodas();
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Listado de visitas obtenido correctamente (" + lista.size() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar visitas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/eventos/all")
    public ResponseEntity<Response> listarTodasConEventos() {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoListarTodas();
            List<VisitaDTO> lista = visitaService.listarTodasConEventos();
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Listado de visitas obtenido correctamente (" + lista.size() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar visitas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> obtenerVisitaPorId(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoVer(id);
            VisitaDTO visita = visitaService.listarPorId(id);
            res.setData(visita);
            res.setCode(200);
            res.setMsg("Visita obtenida correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al encontrar visita: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/tecnico/{id}")
    public ResponseEntity<Response> listarPorTecnico(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoListarPorTecnico(id);
            res.setData(visitaService.listarPorTecnico(id));
            res.setCode(200);
            res.setMsg("Visitas del técnico obtenidas correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener visitas del técnico: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/tecnico/{id}/hoy")
    public ResponseEntity<Response> listarPorTecnicoHoy(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoListarPorTecnico(id);
            res.setData(visitaService.listarPorTecnicoHoy(id));
            res.setCode(200);
            res.setMsg("Visitas del técnico obtenidas correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener visitas del técnico: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/supervisor/{id}")
    public ResponseEntity<Response> listarPorSupervisor(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoListarPorSupervisor(id);
            res.setData(visitaService.listarPorSupervisor(id));
            res.setCode(200);
            res.setMsg("Visitas del supervisor obtenidas correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener visitas del supervisor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/supervisor/{id}/eventos")
    public ResponseEntity<Response> listarPorSupervisorConEventos(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoListarPorSupervisor(id);
            res.setData(visitaService.listarPorSupervisorConEventos(id));
            res.setCode(200);
            res.setMsg("Visitas con eventos del supervisor obtenidas correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener visitas del supervisor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<Response> listarPorEstado(@PathVariable("estado") Visita.VisitState estado) {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoListarTodas();
            res.setData(visitaService.listarPorEstado(estado));
            res.setCode(200);
            res.setMsg("Visitas con estado " + estado + " obtenidas correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar visitas por estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/estado/{estado}/eventos")
    public ResponseEntity<Response> listarPorEstadoConEventos(@PathVariable("estado") Visita.VisitState estado) {
        Response res = new Response();
        try {
            visitaAuth.verificarPermisoListarTodas();
            res.setData(visitaService.listarPorEstadoConEventos(estado));
            res.setCode(200);
            res.setMsg("Visitas y eventos con estado " + estado + " obtenidas correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar visitas por estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Response> listarPorRangoFechas(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fin,
            @RequestParam(name = "estado", required = false) Visita.VisitState estado) {

        Response res = new Response();
        try {
            visitaAuth.verificarPermisoListarTodas();
            res.setData(visitaService.listarPorRangoFechas(inicio, fin, estado));
            res.setCode(200);
            res.setMsg("Visitas obtenidas correctamente para el rango de fechas especificado.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar visitas por rango de fechas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/dashboard/eventos")
    public ResponseEntity<Response> listarPorRangoFechasConEventos(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fin,
            @RequestParam(name = "estado", required = false) Visita.VisitState estado) {

        Response res = new Response();
        try {
            visitaAuth.verificarPermisoListarTodas();
            res.setData(visitaService.listarPorRangoFechasConEventos(inicio, fin, estado));
            res.setCode(200);
            res.setMsg("Visitas y eventos obtenidos correctamente para el rango de fechas especificado.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar visitas por rango de fechas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<Response> listarPorCliente(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            Long userId = clienteService.findUserIdbyClientId(id);
            visitaAuth.verificarPermisoListarPorCliente(userId);
            res.setData(visitaService.listarPorCliente(id));
            res.setCode(200);
            res.setMsg("Visitas del cliente obtenidas correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener visitas del cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

}