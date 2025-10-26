package gt.skynet.semvis.entity.user;

import gt.skynet.semvis.entity.AuditFields;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "role", schema = "semvis_sk")
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private RoleName nombre;

    public Role(RoleName nombre) {
        this.nombre = nombre;
    }
}