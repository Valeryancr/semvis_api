package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.entity.VisitaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitaEventoRepo extends JpaRepository<VisitaEvento, Long> {

    List<VisitaEvento> findByVisita(Visita visita);

    @Query("""
           select e from VisitaEvento e
           where e.visita = :visita and e.tipo = :tipo
           order by e.fecha desc
           """)
    List<VisitaEvento> findByVisitaAndTipo(@Param("visita") Visita visita, @Param("tipo") VisitaEvento.VisitEventType tipo);

    @Query("""
           select e from VisitaEvento e
           where e.visita.id = :visitaId
           order by e.fecha asc
           """)
    List<VisitaEvento> findAllByVisitaId(@Param("visitaId") Long visitaId);

    List<VisitaEvento> findByVisitaIdOrderByFechaAsc(@Param("visitaId") Long visitaId);
}
