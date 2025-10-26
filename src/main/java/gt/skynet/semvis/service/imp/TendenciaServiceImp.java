package gt.skynet.semvis.service.imp;

import gt.skynet.semvis.entity.Visita;
import gt.skynet.semvis.model.TendenciaDTO;
import gt.skynet.semvis.repository.TendenciaRepo;
import gt.skynet.semvis.service.TendenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TendenciaServiceImp implements TendenciaService {

    @Autowired
    private TendenciaRepo repo;

    private LocalDate toLocalDate(Object obj) {
        return obj instanceof java.sql.Date
                ? ((java.sql.Date) obj).toLocalDate()
                : LocalDate.parse(obj.toString());
    }

    @Override
    public List<TendenciaDTO> obtenerTendenciaGeneral(OffsetDateTime inicio, OffsetDateTime fin) {
        if (inicio == null || fin == null) {
            fin = OffsetDateTime.now();
            inicio = fin.minusDays(7);
        }

        return repo.tendenciaGeneral(inicio, fin)
                .stream()
                .map(row -> TendenciaDTO.builder()
                        .fecha(toLocalDate(row[0]))
                        .total(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TendenciaDTO> obtenerTendenciaPorEstado(OffsetDateTime inicio, OffsetDateTime fin) {
        if (inicio == null || fin == null) {
            fin = OffsetDateTime.now();
            inicio = fin.minusDays(7);
        }

        return repo.tendenciaPorEstado(inicio, fin)
                .stream()
                .map(row -> TendenciaDTO.builder()
                        .fecha(toLocalDate(row[0]))
                        .estado(((Visita.VisitState) row[1]).name())
                        .total(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
    }
}