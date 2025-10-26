package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.Visita;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface DashboardRepo extends Repository<Visita, Long>{

    @Query("""
                select count(v) from Visita v
                where v.fechaProgramada between :inicio and :fin
            """)
    long countTotal(@Param("inicio") OffsetDateTime inicio, @Param("fin") OffsetDateTime fin);

    @Query("""
                select count(v) from Visita v
                where v.estado = :estado
                and v.fechaProgramada between :inicio and :fin
            """)
    long countByEstado(
            @Param("estado") Visita.VisitState estado,
            @Param("inicio") OffsetDateTime inicio,
            @Param("fin") OffsetDateTime fin
    );
}