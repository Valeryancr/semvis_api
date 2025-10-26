package gt.skynet.semvis.controller;

import gt.skynet.semvis.model.CatalogoDTO;
import gt.skynet.semvis.service.CatalogosService;
import gt.skynet.semvis.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/catalogos")
public class CatalogosController {

    @Autowired
    private CatalogosService catalogoService;

    @GetMapping("/departamentos")
    public ResponseEntity<Response> listarDepartamentos() {
        Response res = new Response();
        try {
            List<CatalogoDTO> lista = catalogoService.listarDepartamentos();
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Departamentos obtenidos correctamente (" + lista.size() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener departamentos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/municipios")
    public ResponseEntity<Response> listarMunicipios(@RequestParam("deptoId") Integer deptoId) {
        Response res = new Response();
        try {
            List<CatalogoDTO> lista = catalogoService.listarMunicipios(deptoId);
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Municipios del departamento " + deptoId + " obtenidos correctamente (" + lista.size() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener municipios: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PostMapping("/departamentos")
    public ResponseEntity<Response> crearDepartamento(@RequestBody CatalogoDTO dto) {
        Response res = new Response();
        res.setCode(200);
        res.setData(catalogoService.crearDepartamento(dto));
        res.setMsg("Departamento creado correctamente.");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/municipios")
    public ResponseEntity<Response> crearMunicipio(@RequestBody CatalogoDTO dto) {
        Response res = new Response();
        res.setCode(200);
        res.setData(catalogoService.crearMunicipio(dto));
        res.setMsg("Municipio creado correctamente.");
        return ResponseEntity.ok(res);
    }

    @PutMapping("/departamentos/{id}")
    public ResponseEntity<Response> actualizarDepartamento(@PathVariable Integer id, @RequestBody CatalogoDTO dto) {
        catalogoService.actualizarDepartamento(id, dto);
        Response res = new Response();
        res.setCode(200);
        res.setMsg("Departamento actualizado correctamente.");
        return ResponseEntity.ok(res);
    }

    @PutMapping("/municipios/{id}")
    public ResponseEntity<Response> actualizarMunicipio(@PathVariable Integer id, @RequestBody CatalogoDTO dto) {
        catalogoService.actualizarMunicipio(id, dto);
        Response res = new Response();
        res.setCode(200);
        res.setMsg("Municipio actualizado correctamente.");
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/departamentos/{id}")
    public ResponseEntity<Response> eliminarDepartamento(@PathVariable Integer id) {
        catalogoService.eliminarDepartamento(id);
        Response res = new Response();
        res.setCode(200);
        res.setMsg("Departamento eliminado correctamente.");
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/municipios/{id}")
    public ResponseEntity<Response> eliminarMunicipio(@PathVariable Integer id) {
        catalogoService.eliminarMunicipio(id);
        Response res = new Response();
        res.setCode(200);
        res.setMsg("Municipio eliminado correctamente.");
        return ResponseEntity.ok(res);
    }
}