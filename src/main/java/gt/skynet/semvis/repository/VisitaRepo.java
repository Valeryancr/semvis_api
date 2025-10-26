package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface VisitaRepo extends JpaRepository<Visita, Long> {

    List<Visita> findByTecnico(User tecnico);

    List<Visita> findBySupervisor(User supervisor);

    List<Visita> findByEstado(Visita.VisitState estado);

    @Query("""
            select v from Visita v
            where v.cliente.id = :clienteId
            order by v.fechaProgramada desc
            """)
    List<Visita> findByClienteId(@Param("clienteId") Long clienteId);

    @Query("""
            select v from Visita v
            where v.tecnico = :tecnico
            and v.fechaProgramada between :desde and :hasta
            order by v.fechaProgramada asc
            """)
    List<Visita> findByTecnicoAndFechaProgramadaBetween(
            @Param("tecnico") User tecnico,
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta);


    @Query(value = """
            select * from semvis_sk.visita v
            where v.tecnico_id = :tecnicoId
            and v.fecha_programada >= :inicio
            and v.fecha_programada < :fin
            and cast(v.estado as text) not in (:estados)
            order by v.fecha_programada asc
            """, nativeQuery = true)
    List<Visita> findByTecnicoAndFechaProgramadaAndEstadoNotIn(
            @Param("tecnicoId") Long tecnicoId,
            @Param("inicio") OffsetDateTime inicio,
            @Param("fin") OffsetDateTime fin,
            @Param("estados") List<String> estados
    );

    @Query("""
            select v from Visita v
            where v.supervisor = :supervisor
            and v.estado = :estado
            order by v.fechaProgramada desc
            """)
    List<Visita> findBySupervisorAndEstado(
            @Param("supervisor") User supervisor,
            @Param("estado") Visita.VisitState estado);

    @Query("""
            select v from Visita v
            where v.fechaProgramada between :inicio and :fin
            and (:estado is null or v.estado = :estado)
            order by v.fechaProgramada desc
            """)
    List<Visita> findForDashboard(
            @Param("inicio") OffsetDateTime inicio,
            @Param("fin") OffsetDateTime fin,
            @Param("estado") Visita.VisitState estado);

    @Query("""
            SELECT DISTINCT v FROM Visita v
            LEFT JOIN FETCH v.eventos e
            WHERE v.supervisor = :supervisor
            ORDER BY v.fechaProgramada DESC
            """)
    List<Visita> findBySupervisorWithEventos(@Param("supervisor") User supervisor);

    @Query("""
            SELECT DISTINCT v FROM Visita v
            LEFT JOIN FETCH v.eventos e
            ORDER BY v.fechaProgramada DESC
            """)
    List<Visita> findAllWithEventos();

    @Query("""
            SELECT DISTINCT v FROM Visita v
            LEFT JOIN FETCH v.eventos e
            WHERE v.estado = :estado
            ORDER BY v.fechaProgramada DESC
            """)
    List<Visita> findByEstadoWithEventos(@Param("estado") Visita.VisitState estado);

    @Query("""
            SELECT DISTINCT v FROM Visita v
            LEFT JOIN FETCH v.eventos e
            WHERE v.fechaProgramada BETWEEN :inicio AND :fin
            AND (:estado IS NULL OR v.estado = :estado)
            ORDER BY v.fechaProgramada DESC
            """)
    List<Visita> findForDashboardWithEventos(@Param("inicio") OffsetDateTime inicio,
                                             @Param("fin") OffsetDateTime fin,
                                             @Param("estado") Visita.VisitState estado);
}
