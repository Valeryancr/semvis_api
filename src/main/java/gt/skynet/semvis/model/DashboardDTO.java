package gt.skynet.semvis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private long totalVisitas;
    private long programadas;
    private long enCurso;
    private long completadas;
    private long canceladas;
}
