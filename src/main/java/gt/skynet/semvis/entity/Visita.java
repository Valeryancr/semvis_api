package gt.skynet.semvis.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import gt.skynet.semvis.entity.user.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "visita", schema = "semvis_sk")
public class Visita extends AuditFields {

    public enum VisitState {PROGRAMADA, EN_CURSO, COMPLETADA, CANCELADA}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private User tecnico;

    @Column(name = "fecha_programada", nullable = false)
    private OffsetDateTime fechaProgramada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private VisitState estado = VisitState.PROGRAMADA;

    @Column(name = "observaciones_plan")
    private String observacionesPlan;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @OneToMany(mappedBy = "visita", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<VisitaEvento> eventos;
}