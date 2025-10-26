package gt.skynet.semvis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TendenciaDTO {
    private LocalDate fecha;
    private Long total;
    private String estado;
}