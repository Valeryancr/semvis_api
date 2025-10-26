package gt.skynet.semvis.service;

import gt.skynet.semvis.model.CatalogoDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CatalogosService {

    List<CatalogoDTO> listarDepartamentos();

    List<CatalogoDTO> listarMunicipios(Integer deptoId);

    @Transactional
    CatalogoDTO crearDepartamento(CatalogoDTO dto);

    @Transactional
    CatalogoDTO crearMunicipio(CatalogoDTO dto);

    @Transactional
    CatalogoDTO actualizarDepartamento(Integer id, CatalogoDTO dto);

    @Transactional
    CatalogoDTO actualizarMunicipio(Integer id, CatalogoDTO dto);

    @Transactional
    void eliminarDepartamento(Integer id);

    @Transactional
    void eliminarMunicipio(Integer id);
}
