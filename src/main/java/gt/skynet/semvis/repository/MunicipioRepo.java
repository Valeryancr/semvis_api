package gt.skynet.semvis.repository;

import gt.skynet.semvis.entity.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MunicipioRepo extends JpaRepository<Municipio, Integer> {
    List<Municipio> findByDepartamentoId(Integer departamentoId);
}
