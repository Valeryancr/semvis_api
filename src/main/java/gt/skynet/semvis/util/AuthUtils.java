package gt.skynet.semvis.util;


import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.repository.UserRepo;
import gt.skynet.semvis.repository.UserSupervisorRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserSupervisorRepo userSupervisorRepo;

    public User currentUserOrNull(){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getName() == null) return null;
        return userRepo.findByEmail(a.getName()).orElse(null);
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        String username = auth.getName();
        return userRepo.findByUsernameIgnoreCase(username)
                .orElseGet(() -> userRepo.findByEmailIgnoreCase(username).orElse(null));
    }

    public boolean hasRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }

    public boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (String role : roles) {
            if (auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + role))) {
                return true;
            }
        }
        return false;
    }

    public boolean esTecnicoDeSupervisor(Long tecnicoId, Long supervisorId) {
        return userSupervisorRepo.existsBySupervisor_IdAndTecnico_Id(supervisorId, tecnicoId);
    }
}