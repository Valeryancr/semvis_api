package gt.skynet.semvis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private final WebClient webClient;

    @Value("${app.email.brevo.api-key}")
    private String apiKey;

    @Value("${app.email.brevo.sender-name}")
    private String senderName;

    @Value("${app.email.brevo.sender-email}")
    private String senderEmail;

    @Value("${app.email.reset-link.base-url}")
    private String resetLinkBaseUrl;

    @Value("${app.email.login.base-url}")
    private String loginLinkBaseUrl;

    public EmailService(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.brevo.com/v3/smtp")
                .build();
    }

    public void enviarCorreoTemplate(String destinatario, int plantillaId, Map<String, Object> parametros) {
        try {
            Map<String, Object> payload = Map.of(
                    "to", new Object[]{Map.of("email", destinatario)},
                    "templateId", plantillaId,
                    "params", parametros
            );

            LOG.info("Enviando correo con plantilla {} a '{}'", plantillaId, destinatario);

            webClient.post()
                    .uri("/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("api-key", apiKey)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(res -> LOG.info("Correo enviado exitosamente a {} con plantilla {}", destinatario, plantillaId))
                    .doOnError(e -> LOG.error("Error enviando correo a {}: {}", destinatario, e.getMessage()))
                    .block();

        } catch (Exception e) {
            LOG.error("Error inesperado al enviar correo (plantilla {}): {}", plantillaId, e.getMessage(), e);
        }
    }

    public void enviarCorreoReporte(String destinatario, String clienteNombre, String tecnicoNombre,
                                    String fechaVisita, String resumen, String enlacePdf,
                                    Long visitaId) {

        Map<String, Object> parametros = Map.of(
                "cliente_nombre", clienteNombre,
                "tecnico_nombre", tecnicoNombre,
                "fecha_visita", fechaVisita,
                "resumen", resumen,
                "enlace_pdf", enlacePdf,
                "visita_id", visitaId
        );

        enviarCorreoTemplate(destinatario, 1, parametros);
    }

    public void enviarCorreoResetPassword(String destinatario, String token, String nombre) {
        String enlace = resetLinkBaseUrl + "?token=" + token;

        Map<String, Object> parametros = Map.of(
                "reset_link", enlace,
                "company_name", senderName,
                "support_email", senderEmail,
                "nombre", nombre
        );

        enviarCorreoTemplate(destinatario, 2, parametros);
    }

    public void enviarCorreoBienvenida(String destinatario, String nombreUsuario, String passwordTemporal) {
        Map<String, Object> parametros = Map.of(
                "nombre", nombreUsuario,
                "password_temporal", passwordTemporal,
                "login_link", loginLinkBaseUrl,
                "company_name", senderName
        );

        enviarCorreoTemplate(destinatario, 3, parametros);
    }
}