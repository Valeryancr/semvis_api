package gt.skynet.semvis.service;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.model.VisitaDTO;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

public interface VisitaService {
    @Transactional
    VisitaDTO createVisita(VisitaDTO dto);

    @Transactional
    VisitaDTO updateVisita(Long id, VisitaDTO dto);

    @Transactional
    VisitaDTO cambiarEstado(Long id, Visita.VisitState nuevoEstado);

    @Transactional
    VisitaDTO marcarCompletada(Long id);

    List<VisitaDTO> listarTodas();

    List<VisitaDTO> listarTodasConEventos();

    VisitaDTO listarPorId(Long id);

    List<VisitaDTO> listarPorTecnico(Long tecnicoId);

    List<VisitaDTO> listarPorTecnicoHoy(Long tecnicoId);

    List<VisitaDTO> listarPorSupervisor(Long supervisorId);

    List<VisitaDTO> listarPorSupervisorConEventos(Long supervisorId);

    List<VisitaDTO> listarPorEstado(Visita.VisitState estado);

    List<VisitaDTO> listarPorEstadoConEventos(Visita.VisitState estado);

    List<VisitaDTO> listarPorRangoFechas(OffsetDateTime inicio, OffsetDateTime fin, Visita.VisitState estado);

    List<VisitaDTO> listarPorRangoFechasConEventos(OffsetDateTime inicio, OffsetDateTime fin, Visita.VisitState estado);

    boolean esVisitaDeTecnico(Long visitaId, Long tecnicoId);

    boolean usuarioPuedeVerVisita(Long visitaId, Long userId, String rol);

    boolean esVisitaDeCliente(Long visitaId, Long userId);

    List<VisitaDTO> listarPorCliente(Long clienteId);
}
