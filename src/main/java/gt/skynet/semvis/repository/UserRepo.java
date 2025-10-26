package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.user.RoleName;
import gt.skynet.semvis.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query("""
            select u from User u
            where lower(u.email) = lower(:id) or lower(u.username) = lower(:id)
            """)
    Optional<User> findByLoginId(String id);

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.primerNombre) LIKE :term
               OR LOWER(u.apellido1) LIKE :term
               OR LOWER(u.email) LIKE :term
               OR LOWER(u.username) LIKE :term
            """)
    Page<User> buscarPorNombreOEmail(@Param("term") String term, Pageable pageable);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    List<User> findByRolesNombre(RoleName role);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findByEstadoTrue();

    Optional<User> findByResetToken(String resetToken);
}