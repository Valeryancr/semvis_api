package gt.skynet.semvis.service;

import gt.skynet.semvis.model.DashboardDTO;

import java.time.OffsetDateTime;

public interface DashboardService {
    DashboardDTO obtenerResumen(OffsetDateTime inicio, OffsetDateTime fin);
}
