package gt.skynet.semvis.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gt.skynet.semvis.entity.AuditFields;
import gt.skynet.semvis.entity.RefreshToken;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "\"user\"", schema = "semvis_sk")
@JsonIgnoreProperties({"roles","lastLoginAt","createdById","updatedById"})
public class User extends AuditFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "pass_hash", nullable = false)
    private String passHash;

    @Column(name = "primer_nombre", nullable = false)
    private String primerNombre;

    @Column(name = "segundo_nombre")
    private String segundoNombre;

    @Column(name = "apellido1", nullable = false)
    private String apellido1;

    @Column(name = "apellido2")
    private String apellido2;

    @Column(name = "apellido_casada")
    private String apellidoCasada;

    @Column(name = "nombre_completo", insertable = false, updatable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "username", nullable = false, unique = true, columnDefinition = "citext")
    private String username;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_expires")
    private OffsetDateTime resetExpires;

    @Column(name = "login_attempts", nullable = false)
    private Short loginAttempts = 0;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private OffsetDateTime passwordChangedAt;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            schema = "semvis_sk", name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role", referencedColumnName = "nombre")
    )
    private Set<Role> roles = new HashSet<>();

    public Collection<SimpleGrantedAuthority> getGrantedAuthorities() {
        List<SimpleGrantedAuthority> list = new ArrayList<>();
        for (Role r : roles) list.add(new SimpleGrantedAuthority("ROLE_" + r.getNombre().name()));
        return list;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RefreshToken> refreshToken = new ArrayList<>();

    @PrePersist
    public void initPasswordChangeDate() {
        if (passwordChangedAt == null)
            passwordChangedAt = OffsetDateTime.now();
    }

    public String getNombreCorto() {
        return primerNombre + " " + apellido1;
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.lockedUntil = null;
    }

    public void bloquearTemporalmente(int minutos) {
        this.lockedUntil = OffsetDateTime.now().plusMinutes(minutos);
    }

    public boolean isBloqueado() {
        return lockedUntil != null && lockedUntil.isAfter(OffsetDateTime.now());
    }
}
