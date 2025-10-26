package gt.skynet.semvis.service.imp;

import gt.skynet.semvis.config.SecurityConfig;
import gt.skynet.semvis.entity.Cliente;
import gt.skynet.semvis.entity.Departamento;
import gt.skynet.semvis.entity.Direccion;
import gt.skynet.semvis.entity.Municipio;
import gt.skynet.semvis.entity.user.Role;
import gt.skynet.semvis.entity.user.RoleName;
import gt.skynet.semvis.entity.user.User;
import gt.skynet.semvis.model.ClienteDTO;
import gt.skynet.semvis.repository.ClienteRepo;
import gt.skynet.semvis.repository.DepartamentoRepo;
import gt.skynet.semvis.repository.DireccionRepo;
import gt.skynet.semvis.repository.MunicipioRepo;
import gt.skynet.semvis.repository.RoleRepo;
import gt.skynet.semvis.repository.UserRepo;
import gt.skynet.semvis.service.ClienteService;
import gt.skynet.semvis.util.Password;
import gt.skynet.semvis.util.UserName;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ClienteServiceImp implements ClienteService {

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private DireccionRepo direccionRepo;

    @Autowired
    private DepartamentoRepo deptoRepo;

    @Autowired
    private MunicipioRepo municipioRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private UserName userName;

    @Transactional
    @Override
    public Cliente crearCliente(ClienteDTO dto) {
        Departamento dep = deptoRepo.findById(dto.getDireccion().getDepartamentoId())
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
        Municipio mun = municipioRepo.findById(dto.getDireccion().getMunicipioId())
                .orElseThrow(() -> new IllegalArgumentException("Municipio no encontrado"));

        if (!mun.getDepartamento().getId().equals(dep.getId()))
            throw new IllegalArgumentException("El municipio no pertenece al departamento indicado.");

        Direccion direccion = direccionRepo.saveAndFlush(getDireccion(dto, dep, mun));

        User user = null;
        String rawPass = null;

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (userRepo.existsByEmailIgnoreCase(dto.getEmail()))
                throw new IllegalArgumentException("El correo ya está en uso");

            rawPass = (dto.getPasswordPlain() == null || dto.getPasswordPlain().isBlank())
                    ? Password.generate(12)
                    : dto.getPasswordPlain();

            user = crearUsuarioParaCliente(dto, rawPass);
        }

        Cliente cliente = new Cliente();
        cliente.setUser(user);
        cliente.setDireccion(direccion);
        cliente.setNit(dto.getNit());
        cliente.setTelefono(dto.getTelefono());

        if (user != null)
            cliente.setNombre(user.getPrimerNombre() + " " + user.getApellido1());
        else
            cliente.setNombre(dto.getPrimerNombre() != null ? dto.getPrimerNombre() + " " + dto.getApellido1() : "Cliente sin nombre");

        Cliente guardado = clienteRepo.saveAndFlush(cliente);

        dto.setPasswordGenerada(rawPass);

        return clienteRepo.findById(guardado.getId())
                .orElseThrow(() -> new RuntimeException("Error al recuperar cliente recién creado"));
    }

    @Transactional
    @Override
    public Cliente actualizaCliente(Long id, ClienteDTO dto) {
        Cliente cliente = clienteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        Departamento dep = deptoRepo.findById(dto.getDireccion().getDepartamentoId())
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
        Municipio mun = municipioRepo.findById(dto.getDireccion().getMunicipioId())
                .orElseThrow(() -> new IllegalArgumentException("Municipio no encontrado"));
        if (!mun.getDepartamento().getId().equals(dep.getId()))
            throw new IllegalArgumentException("El municipio no pertenece al departamento indicado.");

        if (cliente.getDireccion() == null) {
            new Direccion();
        }
        dto.getDireccion().setDireccionFmt(
                String.format("%s - %s, %s, %s",
                        dto.getDireccion().getLinea1(),
                        dto.getDireccion().getZona(),
                        mun.getNombre(),
                        dep.getNombre()
                )
        );
        Direccion direccion = direccionRepo.save(getDireccion(dto, dep, mun));
        cliente.setDireccion(direccion);

        cliente.setNit(dto.getNit());
        cliente.setTelefono(dto.getTelefono());

        if (cliente.getUser() != null) {
            actualizarUsuarioDesdeDTO(cliente.getUser(), dto);
            userRepo.save(cliente.getUser());
        }

        return clienteRepo.save(cliente);
    }

    private User crearUsuarioParaCliente(ClienteDTO dto, String rawPass) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPrimerNombre(dto.getPrimerNombre());
        user.setSegundoNombre(dto.getSegundoNombre());
        user.setApellido1(dto.getApellido1());
        user.setApellido2(dto.getApellido2());
        user.setApellidoCasada(dto.getApellidoCasada());
        user.setEstado(true);
        user.setMustChangePassword(true);

        user.setPassHash(securityConfig.passwordEncoder().encode(rawPass));

        Role rolCliente = roleRepo.findByNombre(RoleName.CLIENTE)
                .orElseThrow(() -> new IllegalStateException("Rol CLIENTE no existe"));
        user.getRoles().add(rolCliente);

        String base = dto.getUsername() != null && !dto.getUsername().isBlank()
                ? dto.getUsername()
                : dto.getEmail().split("@")[0];
        user.setUsername(userName.generateFrom(base));

        return userRepo.save(user);
    }

    private void actualizarUsuarioDesdeDTO(User user, ClienteDTO dto) {
        if (dto.getPrimerNombre() != null) user.setPrimerNombre(dto.getPrimerNombre());
        if (dto.getSegundoNombre() != null) user.setSegundoNombre(dto.getSegundoNombre());
        if (dto.getApellido1() != null) user.setApellido1(dto.getApellido1());
        if (dto.getApellido2() != null) user.setApellido2(dto.getApellido2());
        if (dto.getApellidoCasada() != null) user.setApellidoCasada(dto.getApellidoCasada());

        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepo.findByEmailIgnoreCase(dto.getEmail()).isPresent())
                throw new IllegalArgumentException("El correo ya está en uso");
            user.setEmail(dto.getEmail());
        }

        if (dto.getUsername() != null && !dto.getUsername().isBlank()
                && !dto.getUsername().equalsIgnoreCase(user.getUsername())) {
            user.setUsername(userName.generateFrom(dto.getUsername()));
        }
    }

    private static Direccion getDireccion(ClienteDTO dto, Departamento dep, Municipio mun) {
        Direccion direccion = new Direccion();
        direccion.setDepartamento(dep);
        direccion.setMunicipio(mun);
        direccion.setZona(dto.getDireccion().getZona() == null ? null : dto.getDireccion().getZona().shortValue());

        if(dto.getDireccion().getId() != null){
            direccion.setId(dto.getDireccion().getId());
        }
        direccion.setLinea1(dto.getDireccion().getLinea1());
        direccion.setLinea2(dto.getDireccion().getLinea2());
        direccion.setNivel(dto.getDireccion().getNivel());
        direccion.setPiso(dto.getDireccion().getPiso());
        direccion.setReferencia(dto.getDireccion().getReferencia());
        direccion.setCodigoPostal(dto.getDireccion().getCodigoPostal());
        direccion.setLat(dto.getDireccion().getLat());
        direccion.setLon(dto.getDireccion().getLon());
        direccion.setDireccionFmt(
                String.format("%s - %s, %s, %s",
                        dto.getDireccion().getLinea1(),
                        dto.getDireccion().getZona(),
                        mun.getNombre(),
                        dep.getNombre()
                )
        );
        return direccion;
    }

    @Override
    public Page<ClienteDTO> findAll(int page, int size) {
        Page<Cliente> clientes = clienteRepo.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        return clientes.map(ClienteDTO::fromEntitySafe);
    }

    @Override
    public ClienteDTO findById(long id){
        Cliente cliente = clienteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return ClienteDTO.fromEntity(cliente);
    }

    @Override
    public boolean verificaPropietario(Long clienteId, Long userId) {
        return clienteRepo.findById(clienteId)
                .map(c -> c.getUser() != null && c.getUser().getId().equals(userId))
                .orElse(false);
    }

    @Override
    public Long findUserIdbyClientId(Long clienteId) {
        return clienteRepo.findUserIdByClientId(clienteId);
    }

    @Override
    public Long findClientIdByUserId(Long userId) {
        return clienteRepo.findClientIdByUserId(userId);
    }
}
