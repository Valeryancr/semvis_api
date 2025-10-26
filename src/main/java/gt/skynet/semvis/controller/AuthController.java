package gt.skynet.semvis.controller;

import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.repository.RefreshTokenRepo;
import gt.skynet.semvis.repository.UserRepo;
import gt.skynet.semvis.security.JwtService;
import gt.skynet.semvis.service.EmailService;
import gt.skynet.semvis.service.UserService;
import gt.skynet.semvis.util.Response;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwt;

    @Autowired
    private RefreshTokenRepo refreshRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    public record LoginReq(String login, String password) {
    }

    public record LoginRes(String token, String username, String email, Long userId, String name) {
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> login(@Valid @RequestBody LoginReq req) {
        Response res = new Response();
        try {
            User user = findByAnyId(req.login())
                    .orElseThrow(() -> new RuntimeException("Credenciales incorrectas."));

            if (!user.getEstado()) {
                throw new RuntimeException("Usuario inactivo. Contacte al administrador.");
            }

            if (userService.isAccountLocked(user)) {
                throw new RuntimeException("Cuenta bloqueada temporalmente. Intente más tarde.");
            }

            if (!encoder.matches(req.password(), user.getPassHash())) {
                userService.incrementLoginAttempts(user);
                throw new RuntimeException("Credenciales incorrectas.");
            }

            if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(OffsetDateTime.now())) {
                userService.resetLoginAttempts(user);
            }

            if (user.isMustChangePassword()) {
                res.setMsg("Debe cambiar su contraseña temporal antes de continuar.");
                res.setCode(206);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(res);
            }

            userService.resetLoginAttempts(user);
            user.setLastLoginAt(OffsetDateTime.now());
            userRepo.save(user);

            String token = jwt.generate(
                    user.getUsername(),
                    Map.of("uid", user.getId(), "email", user.getEmail(), "name", user.getPrimerNombre() + " " + user.getApellido1())
            );

            res.setData(new LoginRes(token, user.getUsername(), user.getEmail(), user.getId(), user.getPrimerNombre() + " " + user.getApellido1()));
            res.setMsg("Inicio de sesión exitoso.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(401);
            res.setMsg("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<Response> refresh(@RequestHeader(value = "Authorization", required = false) String auth) {
        Response res = new Response();
        try {
            if (auth == null || !auth.startsWith("Bearer "))
                throw new RuntimeException("Token faltante o inválido.");

            String token = auth.substring(7);
            String username = jwt.getSubject(token);

            User user = userRepo.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

            String newToken = jwt.generate(
                    user.getUsername(),
                    Map.of("uid", user.getId(), "email", user.getEmail(), "name", user.getPrimerNombre() + " " + user.getApellido1())
            );

            res.setData(new LoginRes(newToken, user.getUsername(), user.getEmail(), user.getId(), user.getPrimerNombre() + " " + user.getApellido1()));
            res.setMsg("Token renovado exitosamente.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(401);
            res.setMsg("Error al renovar token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response> forgotPassword(@RequestBody Map<String, String> body) {
        Response res = new Response();
        try {
            String email = body.get("email");
            if (email == null || email.isBlank())
                throw new RuntimeException("Debe proporcionar un correo electrónico.");

            Optional<User> optUser = userRepo.findByEmailIgnoreCase(email);
            if (optUser.isEmpty()) {
                res.setMsg("Si el correo está registrado, recibirás instrucciones para restablecer tu contraseña.");
                res.setCode(200);
                return ResponseEntity.ok(res);
            }

            User user = userService.generateResetToken(optUser.get());
            emailService.enviarCorreoResetPassword(user.getEmail(), user.getResetToken(), user.getPrimerNombre());

            res.setMsg("Se ha enviado un enlace de recuperación al correo registrado.");
            res.setCode(200);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al generar enlace de recuperación: " + e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response> resetPassword(@RequestBody Map<String, String> body) {
        Response res = new Response();
        try {
            String token = body.get("token");
            String newPassword = body.get("password");

            if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank())
                throw new RuntimeException("Debe proporcionar el token y la nueva contraseña.");

            boolean ok = userService.resetPassword(token, newPassword);
            if (!ok)
                throw new RuntimeException("El enlace de restablecimiento no es válido o ha expirado.");

            res.setMsg("Contraseña restablecida exitosamente.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al restablecer contraseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Response> changePassword(@RequestBody Map<String, String> body) {
        Response res = new Response();
        try {
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");
            User user = userService.getAuthenticatedUser();

            if (!encoder.matches(oldPassword, user.getPassHash()))
                throw new RuntimeException("Contraseña actual incorrecta");

            user.setPassHash(encoder.encode(newPassword));
            user.setMustChangePassword(false);
            user.setPasswordChangedAt(OffsetDateTime.now());
            userRepo.save(user);

            res.setMsg("Contraseña actualizada exitosamente.");
            res.setCode(200);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al cambiar contraseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    @PostMapping("/forced-password-change")
    public ResponseEntity<Response> forceChangePassword(@RequestBody Map<String, String> body) {
        Response res = new Response();
        try {
            String login = body.get("login");
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");

            if (login == null || oldPassword == null || newPassword == null)
                throw new RuntimeException("Datos incompletos.");

            User user = findByAnyId(login)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

            if (!encoder.matches(oldPassword, user.getPassHash()))
                throw new RuntimeException("Contraseña actual incorrecta.");

            user.setPassHash(encoder.encode(newPassword));
            user.setMustChangePassword(false);
            user.setPasswordChangedAt(OffsetDateTime.now());
            userRepo.save(user);

            res.setCode(200);
            res.setMsg("Contraseña actualizada exitosamente. Inicie sesión nuevamente.");
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.setCode(400);
            res.setMsg("Error al cambiar contraseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
    }

    private Optional<User> findByAnyId(String login) {
        if (login == null) return Optional.empty();

        Optional<User> byUsername = userRepo.findByUsernameIgnoreCase(login);
        if (byUsername.isPresent()) return byUsername;

        Optional<User> byEmail = userRepo.findByEmailIgnoreCase(login);
        if (byEmail.isPresent()) return byEmail;

        try {
            long id = Long.parseLong(login);
            return userRepo.findById(id);
        } catch (NumberFormatException ignore) {
            return Optional.empty();
        }
    }
}