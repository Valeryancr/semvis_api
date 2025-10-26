package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.Visita;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface DashboardUsuarioRepo extends Repository<Visita, Long> {

    @Query("""
                select v.tecnico.id as usuarioId,
                    v.tecnico.nombreCompleto as nombreCompleto,
                    'TECNICO' as rol,
                    count(v.id) as totalVisitas,
                    sum(case when v.estado = 'PROGRAMADA' then 1 else 0 end) as programadas,
                    sum(case when v.estado = 'EN_CURSO' then 1 else 0 end) as enCurso,
                    sum(case when v.estado = 'COMPLETADA' then 1 else 0 end) as completadas,
                    sum(case when v.estado = 'CANCELADA' then 1 else 0 end) as canceladas
                from Visita v
                where v.tecnico is not null
                and v.fechaProgramada between :inicio and :fin
                group by v.tecnico.id, v.tecnico.nombreCompleto
                order by completadas desc
            """)
    List<Object[]> resumenPorTecnico(@Param("inicio") OffsetDateTime inicio, @Param("fin") OffsetDateTime fin);

    @Query("""
                select
                    v.supervisor.id as usuarioId,
                    v.supervisor.nombreCompleto as nombreCompleto,
                    'SUPERVISOR' as rol,
                    count(v.id) as totalVisitas,
                    sum(case when v.estado = 'PROGRAMADA' then 1 else 0 end) as programadas,
                    sum(case when v.estado = 'EN_CURSO' then 1 else 0 end) as enCurso,
                    sum(case when v.estado = 'COMPLETADA' then 1 else 0 end) as completadas,
                    sum(case when v.estado = 'CANCELADA' then 1 else 0 end) as canceladas
                from Visita v
                where v.supervisor is not null
                and v.fechaProgramada between :inicio and :fin
                group by v.supervisor.id, v.supervisor.nombreCompleto
                order by completadas desc
            """)
    List<Object[]> resumenPorSupervisor(@Param("inicio") OffsetDateTime inicio, @Param("fin") OffsetDateTime fin);
}