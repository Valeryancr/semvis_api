package gt.skynet.semvis.service;

import gt.skynet.semvis.entity.user.RoleName;
import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.model.UserDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {
    @Transactional
    UserDTO createUser(UserDTO dto, String rawPassword, Set<RoleName> roles);

    @Transactional
    UserDTO updateUser(Long id, UserDTO dto);

    @Transactional
    void resetPassword(Long id, String newPassword);

    @Transactional
    UserDTO toggleEstado(Long id, boolean activo);

    List<UserDTO> findAllByRole(RoleName role);

    List<UserDTO> findAllActive();

    Optional<UserDTO> findByUsername(String username);

    UserDTO findById(Long id);

    List<UserDTO> findAll();

    Page<UserDTO> buscarUsuarios(String q, Pageable pageable);

    @Transactional
    UserDTO updateRoles(Long id, Set<RoleName> roles);

    User getAuthenticatedUser();

    void asignarTecnico(Long supervisorId, Long tecnicoId);

    @Transactional
    void desasignarTecnico(Long supervisorId, Long tecnicoId);

    List<UserDTO> obtenerTecnicosPorSupervisor(Long supervisorId);

    User generateResetToken(User user);

    boolean resetPassword(String token, String newPassword);

    void incrementLoginAttempts(User user);

    void resetLoginAttempts(User user);

    boolean isAccountLocked(User user);

    void lockUserTemporarily(User user, int minutes);

    @Transactional
    void marcarCambioObligatorio(Long id);
}
