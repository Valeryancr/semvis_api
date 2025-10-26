package gt.skynet.semvis.service.imp;

import gt.skynet.semvis.entity.UserSupervisor;
import gt.skynet.semvis.entity.UserSupervisorId;
import gt.skynet.semvis.entity.user.Role;
import gt.skynet.semvis.entity.user.RoleName;
import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.model.UserDTO;
import gt.skynet.semvis.repository.RoleRepo;
import gt.skynet.semvis.repository.UserRepo;
import gt.skynet.semvis.repository.UserSupervisorRepo;
import gt.skynet.semvis.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserSupervisorRepo userSupervisorRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder encoder;

    private static final int MAX_ATTEMPTS = 5;

    private static final int LOCK_MINUTES = 15;

    @Transactional
    @Override
    public UserDTO createUser(UserDTO dto, String rawPassword, Set<RoleName> roles) {
        if (userRepo.existsByEmailIgnoreCase(dto.getEmail()))
            throw new RuntimeException("El correo ya está registrado.");

        if (userRepo.existsByUsernameIgnoreCase(dto.getUsername()))
            throw new RuntimeException("El nombre de usuario ya existe.");

        User user = dto.toEntity();
        user.setPassHash(encoder.encode(rawPassword));

        Set<Role> roleEntities = roles.stream()
                .map(Role::new)
                .collect(Collectors.toSet());
        user.setRoles(roleEntities);

        userRepo.save(user);
        return UserDTO.fromEntity(user);
    }

    @Transactional
    @Override
    public UserDTO updateUser(Long id, UserDTO dto) {
        User existing = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID " + id));

        existing.setEmail(dto.getEmail());
        existing.setPrimerNombre(dto.getPrimerNombre());
        existing.setSegundoNombre(dto.getSegundoNombre());
        existing.setApellido1(dto.getApellido1());
        existing.setApellido2(dto.getApellido2());
        existing.setApellidoCasada(dto.getApellidoCasada());
        existing.setEstado(dto.getEstado());

        userRepo.save(existing);
        return UserDTO.fromEntity(existing);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setPassHash(encoder.encode(newPassword));
        userRepo.save(u);
    }

    @Transactional
    @Override
    public UserDTO toggleEstado(Long id, boolean activo) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setEstado(activo);
        userRepo.save(u);
        return UserDTO.fromEntity(u);
    }

    @Override
    public List<UserDTO> findAllByRole(RoleName role) {
        return userRepo.findByRolesNombre(role)
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> findAllActive() {
        return userRepo.findByEstadoTrue()
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) {
        return userRepo.findByUsernameIgnoreCase(username)
                .map(UserDTO::fromEntity);
    }

    @Override
    public UserDTO findById(Long id) {
        return userRepo.findById(id)
                .map(UserDTO::fromEntity)
                .orElse(null);
    }

    @Override
    public List<UserDTO> findAll() {
        return userRepo.findAll()
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserDTO> buscarUsuarios(String q, Pageable pageable) {
        Page<User> page;

        if (q != null && !q.isBlank()) {
            String term = "%" + q.trim().toLowerCase() + "%";
            page = userRepo.buscarPorNombreOEmail(term, pageable);
        } else {
            page = userRepo.findAll(pageable);
        }

        return page.map(UserDTO::fromEntity);
    }

    @Transactional
    @Override
    public UserDTO updateRoles(Long id, Set<RoleName> roles) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        user.getRoles().clear();
        roles.forEach(roleName -> {
            Optional<Role> role = roleRepo.findByNombre(roleName);
            if (role.isPresent()){
                user.getRoles().add(role.get());
            } else {
                throw new RuntimeException("Rol no encontrado: " + roleName);
            }
        });

        userRepo.save(user);
        return UserDTO.fromEntity(user);
    }

    @Override
    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsernameIgnoreCase(username)
                .or(() -> userRepo.findByEmailIgnoreCase(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado: " + username));
    }

    @Override
    public void asignarTecnico(Long supervisorId, Long tecnicoId) {
        User supervisor = userRepo.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor no encontrado"));
        User tecnico = userRepo.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

        if (supervisorId.equals(tecnicoId))
            throw new IllegalArgumentException("Un usuario no puede supervisarse a sí mismo.");

        if (userSupervisorRepo.existsBySupervisor_IdAndTecnico_Id(supervisorId, tecnicoId))
            throw new IllegalArgumentException("Ya existe la relación supervisor-técnico.");

        UserSupervisor userSupervisor = new UserSupervisor(
                new UserSupervisorId(supervisorId, tecnicoId),
                supervisor,
                tecnico
        );
        userSupervisorRepo.save(userSupervisor);
    }

    @Transactional
    @Override
    public void desasignarTecnico(Long supervisorId, Long tecnicoId) {
        userSupervisorRepo.deleteBySupervisorIdAndTecnicoId(supervisorId, tecnicoId);
    }

    @Override
    public List<UserDTO> obtenerTecnicosPorSupervisor(Long supervisorId) {
        List<Long> tecnicosIds = userSupervisorRepo.findTecnicosIdsBySupervisor(supervisorId);

        if (tecnicosIds.isEmpty()) return List.of();

        return userRepo.findAllById(tecnicosIds).stream()
                .map(UserDTO::fromEntity)
                .toList();
    }

    @Override
    public User generateResetToken(User user) {
        user.setResetToken(UUID.randomUUID().toString());
        user.setResetExpires(OffsetDateTime.now().plusHours(2));
        return userRepo.save(user);
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> optUser = userRepo.findByResetToken(token);
        if (optUser.isEmpty()) return false;

        User user = optUser.get();

        if (user.getResetExpires() == null || user.getResetExpires().isBefore(OffsetDateTime.now()))
            return false;

        user.setPassHash(encoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetExpires(null);
        user.setPasswordChangedAt(OffsetDateTime.now());
        user.setLoginAttempts((short) 0);
        user.setLockedUntil(null);
        userRepo.save(user);
        return true;
    }

    @Override
    public void incrementLoginAttempts(User user) {
        short attempts = (short) (user.getLoginAttempts() + 1);
        user.setLoginAttempts(attempts);
        if (attempts >= MAX_ATTEMPTS) {
            lockUserTemporarily(user, LOCK_MINUTES);
        }
        userRepo.save(user);
    }

    @Override
    public void resetLoginAttempts(User user) {
        user.setLoginAttempts((short) 0);
        user.setLockedUntil(null);
        userRepo.save(user);
    }

    @Override
    public boolean isAccountLocked(User user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now());
    }

    @Override
    public void lockUserTemporarily(User user, int minutes) {
        user.setLockedUntil(OffsetDateTime.now().plusMinutes(minutes));
        userRepo.save(user);
    }

    @Transactional
    @Override
    public void marcarCambioObligatorio(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID " + id));

        user.setMustChangePassword(true);
        userRepo.save(user);
    }
}