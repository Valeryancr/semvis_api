package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.Visita;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface TendenciaRepo extends Repository<Visita, Long> {

    @Query("""
            select cast(v.fechaProgramada as date) as fecha,
            count(v.id) as total
            from Visita v
            where v.fechaProgramada between :inicio and :fin
            group by cast(v.fechaProgramada as date)
            order by fecha asc
            """)
    List<Object[]> tendenciaGeneral(@Param("inicio") OffsetDateTime inicio, @Param("fin") OffsetDateTime fin);

    @Query("""
            select cast(v.fechaProgramada as date) as fecha,
            v.estado as estado,
            count(v.id) as total
            from Visita v
            where v.fechaProgramada between :inicio and :fin
            group by cast(v.fechaProgramada as date), v.estado
            order by fecha asc
            """)
    List<Object[]> tendenciaPorEstado(@Param("inicio") OffsetDateTime inicio, @Param("fin") OffsetDateTime fin);
}
