package gt.skynet.semvis.controller;

import gt.skynet.semvis.entity.Cliente;
import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.model.ClienteDTO;
import gt.skynet.semvis.service.ClienteService;
import gt.skynet.semvis.service.EmailService;
import gt.skynet.semvis.service.UserService;
import gt.skynet.semvis.util.AuthUtils;
import gt.skynet.semvis.util.Response;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import java.nio.file.AccessDeniedException;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthUtils auth;

    @Autowired
    private EmailService emailService;

    private static final Logger LOG = LoggerFactory.getLogger(ClienteController.class);

    @GetMapping
    public ResponseEntity<Response> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                         @RequestParam(name = "size", defaultValue = "20") int size) {
        Response res = new Response();
        try {
            Page<ClienteDTO> clientes = clienteService.findAll(page, size);
            res.setData(clientes);
            res.setMsg("Listado de clientes obtenido correctamente (" + clientes.getTotalElements() + ").");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Response> getClientId(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            Long clientId = clienteService.findClientIdByUserId(id);
            res.setData(clientId);
            res.setMsg("Cliente obtenido correctamente.");
            res.setCode(200);
        } catch (Exception e) {
            res.setCode(404);
            res.setMsg("Error al obtener cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> get(@PathVariable("id") Long id) {
        Response res = new Response();
        try {
            User actual = auth.getAuthenticatedUser();

            if (auth.hasRole("CLIENTE")) {
                boolean permitido = clienteService.verificaPropietario(id, actual.getId());
                if (!permitido) {
                    throw new AccessDeniedException("Sólo puede visualizar sus datos.");
                }
            }
            ClienteDTO cliente = clienteService.findById(id);
            res.setData(cliente);
            res.setMsg("Cliente obtenido correctamente.");
            res.setCode(200);
        } catch (Exception e) {
            res.setCode(404);
            res.setMsg("Error al obtener cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> create(@Valid @RequestBody ClienteDTO dto) {
        Response res = new Response();
        try {
            if (!auth.hasAnyRole("ADMIN", "SUPERVISOR")) {
                throw new AccessDeniedException("Solo administradores o supervisores pueden crear clientes.");
            }

            Cliente cliente = clienteService.crearCliente(dto);

            res.setData(Map.of(
                    "cliente", ClienteDTO.fromEntity(cliente),
                    "passwordGenerada", dto.getPasswordGenerada()
            ));

            if (dto.getPasswordGenerada() != null && cliente != null) {
                try {
                    emailService.enviarCorreoBienvenida(
                            cliente.getUser().getEmail(),
                            cliente.getNombre(),
                            dto.getPasswordGenerada()
                    );
                } catch (Exception ex) {
                    LOG.error("Error al enviar correo de bienvenida: " + ex.getMessage());
                }
            }
            res.setMsg("Cliente creado correctamente.");
            res.setCode(201);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al crear cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> update(@PathVariable("id") Long id, @Valid @RequestBody ClienteDTO dto) {
        Response res = new Response();
        try {
            User actual = auth.getAuthenticatedUser();

            if (auth.hasRole("CLIENTE")) {
                boolean permitido = clienteService.verificaPropietario(id, actual.getId());
                if (!permitido) {
                    throw new AccessDeniedException("No puede modificar datos de otro cliente.");
                }
            }
            Cliente cliente = clienteService.actualizaCliente(id, dto);
            res.setData(ClienteDTO.fromEntity(cliente));
            res.setMsg("Cliente actualizado correctamente.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al actualizar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

}