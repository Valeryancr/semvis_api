package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.UserSupervisor;
import gt.skynet.semvis.entity.UserSupervisorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSupervisorRepo extends JpaRepository<UserSupervisor, UserSupervisorId> {

    boolean existsBySupervisor_IdAndTecnico_Id(Long supervisorId, Long tecnicoId);

    @Query("SELECT us.tecnico.id FROM UserSupervisor us WHERE us.supervisor.id = :supervisorId")
    List<Long> findTecnicosIdsBySupervisor(@Param("supervisorId") Long supervisorId);

    void deleteBySupervisorIdAndTecnicoId(Long supervisorId, Long tecnicoId);
}
