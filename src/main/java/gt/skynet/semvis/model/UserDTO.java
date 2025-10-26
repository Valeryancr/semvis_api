package gt.skynet.semvis.model;

import gt.skynet.semvis.entity.user.Role;
import gt.skynet.semvis.entity.user.RoleName;
import gt.skynet.semvis.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String primerNombre;
    private String segundoNombre;
    private String apellido1;
    private String apellido2;
    private String apellidoCasada;
    private String nombreCompleto;
    private Boolean estado;
    private OffsetDateTime lastLoginAt;
    private Set<RoleName> roles;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private Boolean mustChangePassword;

    public static UserDTO fromEntity(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .primerNombre(user.getPrimerNombre())
                .segundoNombre(user.getSegundoNombre())
                .apellido1(user.getApellido1())
                .apellido2(user.getApellido2())
                .apellidoCasada(user.getApellidoCasada())
                .nombreCompleto(user.getNombreCompleto())
                .estado(user.getEstado())
                .lastLoginAt(user.getLastLoginAt())
                .roles(user.getRoles().stream()
                        .map(Role::getNombre)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setPrimerNombre(this.primerNombre);
        user.setSegundoNombre(this.segundoNombre);
        user.setApellido1(this.apellido1);
        user.setApellido2(this.apellido2);
        user.setApellidoCasada(this.apellidoCasada);
        user.setEstado(this.estado != null ? this.estado : true);
        user.setMustChangePassword(this.mustChangePassword != null && this.mustChangePassword);
        return user;
    }
}
