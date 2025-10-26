package gt.skynet.semvis.controller;

import gt.skynet.semvis.entity.user.RoleName;
import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.model.UserDTO;
import gt.skynet.semvis.service.UserService;
import gt.skynet.semvis.util.AuthUtils;
import gt.skynet.semvis.util.Response;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthUtils auth;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> createUser(@Valid @RequestBody UserDTO dto,
                                               @RequestParam String password,
                                               @RequestParam(required = false) Set<RoleName> roles) {
        Response res = new Response();

        try {
            if (!auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
                throw new AccessDeniedException("Solo administradores o supervisores pueden crear nuevos usuarios.");
            }

            if (roles == null || roles.isEmpty())
                roles = Set.of(RoleName.TECNICO);

            res.setData(userService.createUser(dto, password, roles));
            res.setMsg("Usuario creado correctamente.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al crear usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        Response res = new Response();
        try {
            User actual = auth.getAuthenticatedUser();
            if (!auth.hasAnyRole("ADMIN") && !actual.getId().equals(id)) {
                throw new AccessDeniedException("No puede modificar datos de otro usuario.");
            }

            res.setData(userService.updateUser(id, dto));
            res.setMsg("Usuario actualizado correctamente.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al actualizar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Response> resetPassword(@PathVariable("id") Long id, @RequestParam("newPassword") String newPassword) {
        Response res = new Response();
        try {
            User actual = auth.getAuthenticatedUser();
            if (!auth.hasAnyRole("ADMIN") && !actual.getId().equals(id)) {
                throw new AccessDeniedException("No puede restablecer la contraseña de otro usuario.");
            }

            userService.resetPassword(id, newPassword);
            res.setCode(200);
            res.setMsg("Contraseña restablecida correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al restablecer contraseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Response> toggleEstado(@PathVariable("id") Long id, @RequestParam("activo") boolean activo) {
        Response res = new Response();
        try {
            if (!auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
                throw new AccessDeniedException("Solo administradores o supervisores pueden cambiar el estado de usuarios.");
            }
            String currentUsername = auth.getAuthenticatedUser().getUsername();
            UserDTO user = userService.findById(id);

            if (!activo && user.getUsername().equalsIgnoreCase(currentUsername))
                throw new RuntimeException("No puedes desactivar tu propio usuario.");

            res.setData(userService.toggleEstado(id, activo));
            res.setCode(200);
            res.setMsg(activo ? "Usuario activado." : "Usuario desactivado.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al cambiar estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> findById(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            User actual = auth.getAuthenticatedUser();
            if (!auth.hasAnyRole("ADMIN", "SUPERVISOR") && !actual.getId().equals(id)) {
                throw new AccessDeniedException("No puede consultar información de otro usuario.");
            }

            res.setData(userService.findById(id));
            res.setCode(200);
            res.setMsg("Usuario encontrado.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping
    public ResponseEntity<Response> findAll() {
        Response res = new Response();
        try {
            List<UserDTO> lista = userService.findAll();
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Listado de usuarios obtenido correctamente (" + lista.size() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar usuarios: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<Response> listarUsuarios(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) String q) {

        Response res = new Response();
        try {
            if (!auth.hasAnyRole("ADMIN")) {
                throw new AccessDeniedException("No tiene permisos para listar usuarios.");
            }

            PageRequest pageable = PageRequest.of(page, size, Sort.by("primerNombre").ascending());
            Page<UserDTO> usuarios = userService.buscarUsuarios(q, pageable);

            res.setData(usuarios);
            res.setMsg("Listado de usuarios obtenido correctamente (" + usuarios.getTotalElements() + ").");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener listado de usuarios: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<Response> findAllActive() {
        Response res = new Response();
        try {
            List<UserDTO> lista = userService.findAllActive();
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Usuarios activos obtenidos correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar usuarios activos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/rol/{rol}")
    public ResponseEntity<Response> findByRole(@PathVariable("rol") RoleName rol) {
        Response res = new Response();
        try {
            List<UserDTO> lista = userService.findAllByRole(rol);
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Usuarios con rol " + rol + " obtenidos correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al listar usuarios por rol: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<Response> updateRoles(@PathVariable("id") Long id, @RequestParam Set<RoleName> roles) {
        Response res = new Response();
        try {
            res.setData(userService.updateRoles(id, roles));
            res.setCode(200);
            res.setMsg("Roles actualizados correctamente.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al actualizar roles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/{supervisorId}/tecnicos")
    public ResponseEntity<Response> listarTecnicosPorSupervisor(@PathVariable Long supervisorId) {
        Response res = new Response();
        try {
            List<UserDTO> lista = userService.obtenerTecnicosPorSupervisor(supervisorId);
            res.setData(lista);
            res.setCode(200);
            res.setMsg("Técnicos del supervisor obtenidos correctamente (" + lista.size() + ").");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener técnicos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PostMapping("/{supervisorId}/asignar-tecnico/{tecnicoId}")
    public ResponseEntity<Response> asignarTecnico(@PathVariable Long supervisorId, @PathVariable Long tecnicoId) {
        Response res = new Response();
        try {
            userService.asignarTecnico(supervisorId, tecnicoId);
            res.setMsg("Técnico asignado correctamente.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al asignar técnico: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @DeleteMapping("/{supervisorId}/desasignar-tecnico/{tecnicoId}")
    public ResponseEntity<Response> desasignarTecnico(@PathVariable Long supervisorId, @PathVariable Long tecnicoId) {
        Response res = new Response();
        try {
            userService.desasignarTecnico(supervisorId, tecnicoId);
            res.setMsg("Técnico desasignado correctamente.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al desasignar técnico: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PutMapping("/{id}/force-change-password")
    public ResponseEntity<Response> forzarCambioPassword(@PathVariable Long id) {
        Response res = new Response();
        try {
            if (!auth.hasAnyRole("ADMIN")) {
                throw new AccessDeniedException("No tiene permisos para forzar cambios de contraseña.");
            }

            userService.marcarCambioObligatorio(id);
            res.setCode(200);
            res.setMsg("Cambio de contraseña forzado correctamente para el usuario ID " + id);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            res.setCode(404);
            res.setMsg("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);

        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al forzar cambio de contraseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }
}