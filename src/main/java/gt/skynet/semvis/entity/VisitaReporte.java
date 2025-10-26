package gt.skynet.semvis.entity;

import gt.skynet.semvis.entity.user.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "visita_reporte", schema = "semvis_sk")
public class VisitaReporte extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "visita_id")
    private Visita visita;

    @Column(name = "storage_url")
    private String storageUrl;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "pdf_blob", columnDefinition = "bytea")
    private byte[] pdfBlob;

    @Column(name = "enviado_email", nullable = false)
    private Boolean enviadoEmail = false;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_carga_id")
    private User usuarioCarga;
}