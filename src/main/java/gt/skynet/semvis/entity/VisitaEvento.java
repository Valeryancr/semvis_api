package gt.skynet.semvis.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "visita_evento", schema = "semvis_sk")
public class VisitaEvento extends AuditFields {

    public enum VisitEventType {CHECKIN, CHECKOUT}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "visita_id")
    @JsonBackReference
    private Visita visita;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private VisitEventType tipo;

    @Column(nullable = false)
    private OffsetDateTime fecha;

    private Double lat;
    private Double lon;

    private String nota;

    @Column(name = "created_by")
    private String createdBy;
}
