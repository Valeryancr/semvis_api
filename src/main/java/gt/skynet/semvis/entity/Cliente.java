package gt.skynet.semvis.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gt.skynet.semvis.entity.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cliente", schema = "semvis_sk")
@JsonIgnoreProperties({"createdById","updatedById"})
public class Cliente extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false)
    private String nombre;

    private String nit;
    private String telefono;

    @ManyToOne
    @JoinColumn(name = "direccion_id")
    private Direccion direccion;

    @PrePersist
    @PreUpdate
    public void actualizarNombreDesdeUsuario() {
        if (user != null) {
            String primerNombre = user.getPrimerNombre() != null ? user.getPrimerNombre().trim() : "";
            String apellido1 = user.getApellido1() != null ? user.getApellido1().trim() : "";
            this.nombre = (primerNombre + " " + apellido1).trim();
        }
    }
}