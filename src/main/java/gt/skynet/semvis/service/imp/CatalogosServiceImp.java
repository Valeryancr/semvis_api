package gt.skynet.semvis.service.imp;

import gt.skynet.semvis.entity.Departamento;
import gt.skynet.semvis.entity.Municipio;
import gt.skynet.semvis.model.CatalogoDTO;
import gt.skynet.semvis.repository.DepartamentoRepo;
import gt.skynet.semvis.repository.MunicipioRepo;
import gt.skynet.semvis.service.CatalogosService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogosServiceImp implements CatalogosService {

    @Autowired
    private DepartamentoRepo depRepo;

    @Autowired
    private MunicipioRepo munRepo;

    @Override
    public List<CatalogoDTO> listarDepartamentos() {
        return depRepo.findAll()
                .stream()
                .map(CatalogoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CatalogoDTO> listarMunicipios(Integer deptoId) {
        if (deptoId == null) {
            throw new IllegalArgumentException("Debe especificar el ID del departamento.");
        }

        return munRepo.findByDepartamentoId(deptoId.intValue())
                .stream()
                .map(CatalogoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CatalogoDTO crearDepartamento(CatalogoDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del departamento es obligatorio.");

        Departamento dep = new Departamento();
        dep.setNombre(dto.getNombre().trim());
        dep = depRepo.save(dep);
        return CatalogoDTO.fromEntity(dep);
    }

    @Transactional
    @Override
    public CatalogoDTO crearMunicipio(CatalogoDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del municipio es obligatorio.");

        if (dto.getDepartamentoId() !=  null) {
            Departamento dep = depRepo.findById(dto.getDepartamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado."));

            Municipio mun = new Municipio();
            mun.setNombre(dto.getNombre().trim());
            mun.setDepartamento(dep);
            mun = munRepo.save(mun);
            return CatalogoDTO.fromEntity(mun);
        } else {
            throw new IllegalArgumentException("El municipio debe pertenecer a un departamento.");
        }

    }

    @Transactional
    @Override
    public CatalogoDTO actualizarDepartamento(Integer id, CatalogoDTO dto) {
        Departamento dep = depRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado."));

        dep.setNombre(dto.getNombre().trim());
        dep = depRepo.save(dep);
        return CatalogoDTO.fromEntity(dep);
    }

    @Transactional
    @Override
    public CatalogoDTO actualizarMunicipio(Integer id, CatalogoDTO dto) {
        Municipio mun = munRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Municipio no encontrado."));

        if (dto.getDepartamentoId() != null) {
            Departamento dep = depRepo.findById(dto.getDepartamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado."));
            mun.setDepartamento(dep);
        }

        mun.setNombre(dto.getNombre().trim());
        mun = munRepo.save(mun);
        return CatalogoDTO.fromEntity(mun);
    }

    @Transactional
    @Override
    public void eliminarDepartamento(Integer id) {
        if (!depRepo.existsById(id.intValue()))
            throw new IllegalArgumentException("Departamento no encontrado.");
        depRepo.deleteById(id.intValue());
    }

    @Transactional
    @Override
    public void eliminarMunicipio(Integer id) {
        if (!munRepo.existsById(id.intValue()))
            throw new IllegalArgumentException("Municipio no encontrado.");
        munRepo.deleteById(id.intValue());
    }
}
