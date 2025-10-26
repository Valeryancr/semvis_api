package gt.skynet.semvis.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifica reglas de acceso definidas en SecurityConfig y las utilidades *AuthUtils.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("🧭 Dashboard - ADMIN tiene acceso")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void dashboardAdminAccess() throws Exception {
        mvc.perform(get("/dashboard/resumen"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("🧭 Dashboard - SUPERVISOR tiene acceso")
    @WithMockUser(username = "supervisor", roles = {"SUPERVISOR"})
    void dashboardSupervisorAccess() throws Exception {
        mvc.perform(get("/dashboard/resumen"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("🧭 Dashboard - TECNICO no tiene acceso")
    @WithMockUser(username = "tecnico", roles = {"TECNICO"})
    void dashboardTechnicianForbidden() throws Exception {
        mvc.perform(get("/dashboard/resumen"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("🧭 Dashboard - CLIENTE no tiene acceso")
    @WithMockUser(username = "cliente", roles = {"CLIENTE"})
    void dashboardClientForbidden() throws Exception {
        mvc.perform(get("/dashboard/resumen"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("🗓️ Visitas - ADMIN puede crear visitas")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void visitasAdminCreate() throws Exception {
        mvc.perform(post("/visitas")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("{\"clienteId\":1,\"tecnicoId\":1,\"fechaProgramada\":\"2025-10-22T10:00:00Z\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("🗓️ Visitas - SUPERVISOR puede crear visitas")
    @WithMockUser(username = "sup", roles = {"SUPERVISOR"})
    void visitasSupervisorCreate() throws Exception {
        mvc.perform(post("/visitas")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("{\"clienteId\":1,\"tecnicoId\":1,\"fechaProgramada\":\"2025-10-22T10:00:00Z\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("🗓️ Visitas - CLIENTE no puede crear visitas")
    @WithMockUser(username = "cliente", roles = {"CLIENTE"})
    void visitasClientCreateForbidden() throws Exception {
        mvc.perform(post("/visitas")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("{\"clienteId\":1,\"tecnicoId\":1,\"fechaProgramada\":\"2025-10-22T10:00:00Z\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("🗓️ Visitas - TECNICO no puede crear visitas")
    @WithMockUser(username = "tecnico", roles = {"TECNICO"})
    void visitasTechnicianCreateForbidden() throws Exception {
        mvc.perform(post("/visitas")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("{\"clienteId\":1,\"tecnicoId\":1,\"fechaProgramada\":\"2025-10-22T10:00:00Z\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("📄 Reporte - CLIENTE puede ver su PDF (propio)")
    @WithMockUser(username = "clientetest", roles = {"CLIENTE"})
    void reporteClienteVerPropio() throws Exception {
        mvc.perform(get("/visitas/reportes/1/pdf"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("📄 Reporte - CLIENTE no puede ver otros PDF")
    @WithMockUser(username = "cliente", roles = {"CLIENTE"})
    void reporteClienteVerOtro() throws Exception {
        mvc.perform(get("/visitas/reportes/99/pdf"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("📄 Reporte - ADMIN puede ver cualquier PDF")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void reporteAdminVerTodos() throws Exception {
        mvc.perform(get("/visitas/reportes/1/pdf"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("📊 Tendencias - SUPERVISOR tiene acceso")
    @WithMockUser(username = "supervisor", roles = {"SUPERVISOR"})
    void tendenciasSupervisorAccess() throws Exception {
        mvc.perform(get("/dashboard/tendencias/general"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("📊 Tendencias - TECNICO no tiene acceso")
    @WithMockUser(username = "tecnico", roles = {"TECNICO"})
    void tendenciasTechnicianForbidden() throws Exception {
        mvc.perform(get("/dashboard/tendencias/general"))
                .andExpect(status().isForbidden());
    }
}

