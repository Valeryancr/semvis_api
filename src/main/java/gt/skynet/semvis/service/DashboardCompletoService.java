package gt.skynet.semvis.service;

import gt.skynet.semvis.model.DashboardCompletoDTO;

import java.time.OffsetDateTime;

public interface DashboardCompletoService {
    DashboardCompletoDTO obtenerDashboardCompleto(OffsetDateTime inicio, OffsetDateTime fin);
}
