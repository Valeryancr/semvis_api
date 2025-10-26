package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepo extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUserId(Long userId);

    @Query("SELECT c.id FROM Cliente c WHERE c.user.id = :userId")
    Long findClientIdByUserId(@Param("userId") Long userId);

    @Query("SELECT u.user.id FROM Cliente u WHERE u.id = :clientId")
    Long findUserIdByClientId(@Param("clientId") Long clientId);
}
