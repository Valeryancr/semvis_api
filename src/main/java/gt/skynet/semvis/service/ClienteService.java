package gt.skynet.semvis.service;

import gt.skynet.semvis.entity.Cliente;
import gt.skynet.semvis.model.ClienteDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

public interface ClienteService {
    @Transactional
    Cliente crearCliente(ClienteDTO dto);

    @Transactional
    Cliente actualizaCliente(Long id, ClienteDTO dto);

    Page<ClienteDTO> findAll(int page, int size);

    ClienteDTO findById(long id);

    boolean verificaPropietario(Long clienteId, Long userId);

    Long findUserIdbyClientId(Long clienteId);

    Long findClientIdByUserId(Long userId);
}
