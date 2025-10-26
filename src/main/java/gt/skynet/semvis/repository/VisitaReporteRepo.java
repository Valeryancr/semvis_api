package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.VisitaReporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitaReporteRepo extends JpaRepository<VisitaReporte, Long> {

    @Query("""
            select r from VisitaReporte r
            where r.visita.id = :visitaId
            order by r.createdAt desc
            """)
    List<VisitaReporte> findByVisitaId(@Param("visitaId") Long visitaId);

    @Query("""
                SELECT r
                FROM VisitaReporte r
                LEFT JOIN r.visita v
                LEFT JOIN v.cliente c
                LEFT JOIN v.tecnico t
                LEFT JOIN r.usuarioCarga uc
                WHERE (
                    COALESCE(CAST(:term AS string), '') = ''
                    OR (
                        LOWER(COALESCE(r.nombreArchivo, '')) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))
                        OR LOWER(COALESCE(uc.username, '')) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))
                        OR LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))
                        OR LOWER(COALESCE(t.primerNombre, '')) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))
                    )
                )
            """)
    Page<VisitaReporte> buscarTodos(@Param("term") String term, Pageable pageable);

    @Query("""
                SELECT r
                FROM VisitaReporte r
                LEFT JOIN r.visita v
                LEFT JOIN v.cliente c
                LEFT JOIN v.tecnico t
                LEFT JOIN v.supervisor s
                LEFT JOIN r.usuarioCarga uc
                WHERE s.id = :supervisorId
                  AND (
                    COALESCE(CAST(:term AS string), '') = ''
                    OR (
                        LOWER(COALESCE(r.nombreArchivo, '')) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))
                        OR LOWER(COALESCE(uc.username, '')) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))
                        OR LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))
                        OR LOWER(COALESCE(t.primerNombre, '')) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))
                    )
                  )
            """)
    Page<VisitaReporte> buscarPorSupervisor(@Param("supervisorId") Long supervisorId, @Param("term") String term, Pageable pageable);
}
