package gt.skynet.semvis.service;

import gt.skynet.semvis.model.TendenciaDTO;

import java.time.OffsetDateTime;
import java.util.List;

public interface TendenciaService {
    List<TendenciaDTO> obtenerTendenciaGeneral(OffsetDateTime inicio, OffsetDateTime fin);

    List<TendenciaDTO> obtenerTendenciaPorEstado(OffsetDateTime inicio, OffsetDateTime fin);
}
