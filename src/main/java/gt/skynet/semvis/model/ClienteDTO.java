package gt.skynet.semvis.model;

import gt.skynet.semvis.entity.Cliente;
import gt.skynet.semvis.entity.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    private Long id;

    @NotBlank
    private String primerNombre;
    private String segundoNombre;

    @NotBlank
    private String apellido1;
    private String apellido2;
    private String apellidoCasada;
    private String nombreCompleto;

    private String nit;
    private String telefono;

    @NotNull
    private DireccionDTO direccion;

    private String email;
    private String passwordPlain;
    private String username;
    private String passwordGenerada;
    private boolean estado;

    public static ClienteDTO fromEntity(Cliente cliente) {
        if (cliente == null) return null;

        User user = cliente.getUser();

        String nombreFinal = cliente.getUser() != null
                ? cliente.getUser().getPrimerNombre() + " " + cliente.getUser().getApellido1()
                : cliente.getNombre();

        return ClienteDTO.builder()
                .id(cliente.getId())
                .primerNombre(user != null ? user.getPrimerNombre() : null)
                .segundoNombre(user != null ? user.getSegundoNombre() : null)
                .apellido1(user != null ? user.getApellido1() : null)
                .apellido2(user != null ? user.getApellido2() : null)
                .apellidoCasada(user != null ? user.getApellidoCasada() : null)
                .nit(cliente.getNit())
                .telefono(cliente.getTelefono())
                .direccion(DireccionDTO.fromEntity(cliente.getDireccion()))
                .email(user != null ? user.getEmail() : null)
                .username(user != null ? user.getUsername() : null)
                .build();
    }

    public static ClienteDTO fromEntitySafe(Cliente cliente) {
        if (cliente == null) return null;

        User user = cliente.getUser();

        String nombreFinal = cliente.getUser() != null
                ? cliente.getUser().getPrimerNombre() + " " + cliente.getUser().getApellido1()
                : cliente.getNombre();

        assert user != null;
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nombreCompleto(user.getNombreCompleto())
                .primerNombre(user.getPrimerNombre())
                .segundoNombre(user.getSegundoNombre())
                .apellido1(user.getApellido1())
                .apellido2(user.getApellido2())
                .apellidoCasada(user.getApellidoCasada())
                .nit(cliente.getNit())
                .telefono(cliente.getTelefono())
                .direccion(DireccionDTO.fromEntity(cliente.getDireccion()))
                .email(user.getEmail())
                .username(user.getUsername())
                .estado(user.getEstado())
                .build();
    }

    public Cliente toEntity() {
        Cliente cliente = new Cliente();
        cliente.setId(this.id);
        cliente.setNit(this.nit);
        cliente.setTelefono(this.telefono);
        return cliente;
    }
}
