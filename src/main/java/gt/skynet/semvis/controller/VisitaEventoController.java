package gt.skynet.semvis.controller;

import gt.skynet.semvis.model.VisitaEventoDTO;
import gt.skynet.semvis.service.VisitaEventoService;
import gt.skynet.semvis.util.Response;
import gt.skynet.semvis.util.VisitaEventoAuthUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/visitas/eventos")
public class VisitaEventoController {

    @Autowired
    private VisitaEventoService eventoService;

    @Autowired
    private VisitaEventoAuthUtils visitaEventoAuth;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> registrarEvento(@Valid @RequestBody VisitaEventoDTO dto) {
        Response res = new Response();
        try {
            visitaEventoAuth.verificarPermisoRegistrar(dto.getVisitaId());
            res.setData(eventoService.registrarEvento(dto));
            res.setMsg("Evento registrado correctamente.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al registrar evento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/visita/{id}")
    public ResponseEntity<Response> listarPorVisita(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            visitaEventoAuth.verificarPermisoVerEventos(id);
            List<VisitaEventoDTO> lista = eventoService.listarEventosPorVisita(id);
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Eventos obtenidos correctamente (" + lista.size() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar eventos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }
}