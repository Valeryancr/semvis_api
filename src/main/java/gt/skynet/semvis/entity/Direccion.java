package gt.skynet.semvis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "direccion", schema = "semvis_sk")
public class Direccion extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    @Column
    private Short zona;

    @Column(nullable = false)
    private String linea1;

    private String linea2;

    private String nivel;

    private String piso;

    private String referencia;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    private Double lat;

    private Double lon;

    @Column(name = "direccion_fmt", nullable = false)
    private String direccionFmt;
}