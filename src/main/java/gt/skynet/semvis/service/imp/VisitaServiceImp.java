package gt.skynet.semvis.service.imp;

import gt.skynet.semvis.entity.Cliente;
import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.model.VisitaDTO;
import gt.skynet.semvis.repository.ClienteRepo;
import gt.skynet.semvis.repository.UserRepo;
import gt.skynet.semvis.repository.VisitaRepo;
import gt.skynet.semvis.service.VisitaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisitaServiceImp implements VisitaService {

    @Autowired
    private VisitaRepo visitaRepo;

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private UserRepo userRepo;

    @Transactional
    @Override
    public VisitaDTO createVisita(VisitaDTO dto) {
        Cliente cliente = clienteRepo.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID " + dto.getClienteId()));

        User supervisor = dto.getSupervisorId() != null
                ? userRepo.findById(dto.getSupervisorId()).orElse(null)
                : null;

        User tecnico = dto.getTecnicoId() != null
                ? userRepo.findById(dto.getTecnicoId()).orElse(null)
                : null;

        Visita visita = dto.toEntity(cliente, supervisor, tecnico);
        visitaRepo.save(visita);
        return VisitaDTO.fromEntity(visita);
    }

    @Transactional
    @Override
    public VisitaDTO updateVisita(Long id, VisitaDTO dto) {
        Visita existente = visitaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada con ID " + id));

        if (dto.getClienteId() != null)
            existente.setCliente(clienteRepo.findById(dto.getClienteId()).orElse(existente.getCliente()));

        if (dto.getSupervisorId() != null)
            existente.setSupervisor(userRepo.findById(dto.getSupervisorId()).orElse(existente.getSupervisor()));

        if (dto.getTecnicoId() != null)
            existente.setTecnico(userRepo.findById(dto.getTecnicoId()).orElse(existente.getTecnico()));

        existente.setFechaProgramada(dto.getFechaProgramada());
        existente.setObservacionesPlan(dto.getObservacionesPlan());

        visitaRepo.save(existente);
        return VisitaDTO.fromEntity(existente);
    }

    @Transactional
    @Override
    public VisitaDTO cambiarEstado(Long id, Visita.VisitState nuevoEstado) {
        Visita visita = visitaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada con ID " + id));

        visita.setEstado(nuevoEstado);

        if (nuevoEstado == Visita.VisitState.COMPLETADA)
            visita.setCompletedAt(OffsetDateTime.now());

        visitaRepo.save(visita);
        return VisitaDTO.fromEntity(visita);
    }

    @Transactional
    @Override
    public VisitaDTO marcarCompletada(Long id) {
        return cambiarEstado(id, Visita.VisitState.COMPLETADA);
    }

    @Override
    public List<VisitaDTO> listarTodas() {
        return visitaRepo.findAll()
                .stream()
                .map(VisitaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<VisitaDTO> listarTodasConEventos() {
        return visitaRepo.findAllWithEventos()
                .stream()
                .map(VisitaDTO::fromEntityWithEvents)
                .collect(Collectors.toList());
    }

    @Override
    public VisitaDTO listarPorId(Long id) {
        return visitaRepo.findById(id)
                .map(VisitaDTO::fromEntity)
                .orElse(null);
    }

    @Override
    public List<VisitaDTO> listarPorTecnico(Long tecnicoId) {
        User tecnico = userRepo.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado con ID " + tecnicoId));

        return visitaRepo.findByTecnico(tecnico)
                .stream()
                .map(VisitaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<VisitaDTO> listarPorTecnicoHoy(Long tecnicoId) {
        ZoneId zone = ZoneId.of("America/Guatemala");

        OffsetDateTime inicio = LocalDate.now(zone)
                .atStartOfDay(zone)
                .toOffsetDateTime();

        OffsetDateTime fin = inicio.plusDays(1);

        List<String> estadosExcluidos = List.of(
                Visita.VisitState.CANCELADA.name(),
                Visita.VisitState.COMPLETADA.name()
        );

        return visitaRepo.findByTecnicoAndFechaProgramadaAndEstadoNotIn(
                        tecnicoId, inicio, fin, estadosExcluidos)
                .stream()
                .map(VisitaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<VisitaDTO> listarPorSupervisor(Long supervisorId) {
        User supervisor = userRepo.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor no encontrado con ID " + supervisorId));

        return visitaRepo.findBySupervisor(supervisor)
                .stream()
                .map(VisitaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<VisitaDTO> listarPorSupervisorConEventos(Long supervisorId) {
        User supervisor = userRepo.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor no encontrado con ID " + supervisorId));

        return visitaRepo.findBySupervisorWithEventos(supervisor)
                .stream()
                .map(VisitaDTO::fromEntityWithEvents)
                .collect(Collectors.toList());
    }

    @Override
    public List<VisitaDTO> listarPorEstado(Visita.VisitState estado) {
        return visitaRepo.findByEstado(estado)
                .stream()
                .map(VisitaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<VisitaDTO> listarPorEstadoConEventos(Visita.VisitState estado) {
        return visitaRepo.findByEstadoWithEventos(estado)
                .stream()
                .map(VisitaDTO::fromEntityWithEvents)
                .collect(Collectors.toList());
    }

    @Override
    public List<VisitaDTO> listarPorRangoFechas(OffsetDateTime inicio, OffsetDateTime fin, Visita.VisitState estado) {
        return visitaRepo.findForDashboard(inicio, fin, estado)
                .stream()
                .map(VisitaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<VisitaDTO> listarPorRangoFechasConEventos(OffsetDateTime inicio, OffsetDateTime fin, Visita.VisitState estado) {
        return visitaRepo.findForDashboardWithEventos(inicio, fin, estado)
                .stream()
                .map(VisitaDTO::fromEntityWithEvents)
                .collect(Collectors.toList());
    }

    @Override
    public boolean esVisitaDeTecnico(Long visitaId, Long tecnicoId) {
        return visitaRepo.findById(visitaId)
                .map(v -> v.getTecnico() != null && v.getTecnico().getId().equals(tecnicoId))
                .orElse(false);
    }

    @Override
    public boolean usuarioPuedeVerVisita(Long visitaId, Long userId, String rol) {
        return visitaRepo.findById(visitaId)
                .map(v -> {
                    switch (rol) {
                        case "TECNICO":
                            return v.getTecnico() != null && v.getTecnico().getId().equals(userId);
                        case "CLIENTE":
                            return v.getCliente() != null && v.getCliente().getUser() != null
                                    && v.getCliente().getUser().getId().equals(userId);
                        default:
                            return false;
                    }
                })
                .orElse(false);
    }

    @Override
    public boolean esVisitaDeCliente(Long visitaId, Long userId) {
        return visitaRepo.findById(visitaId)
                .map(v -> v.getCliente() != null
                        && v.getCliente().getUser() != null
                        && v.getCliente().getUser().getId().equals(userId))
                .orElse(false);
    }

    @Override
    public List<VisitaDTO> listarPorCliente(Long clienteId) {
        return visitaRepo.findByClienteId(clienteId)
                .stream()
                .map(VisitaDTO::fromEntityForCliente)
                .collect(Collectors.toList());
    }
}
