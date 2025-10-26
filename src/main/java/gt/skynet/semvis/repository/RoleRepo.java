package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.user.Role;
import gt.skynet.semvis.entity.user.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepo extends JpaRepository<Role, Integer> {
    Optional<Role> findByNombre(RoleName nombre);
}
