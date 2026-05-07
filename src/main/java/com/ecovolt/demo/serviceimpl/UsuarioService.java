package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.ConfiguracionNotificacionesDto;
import com.ecovolt.demo.dtos.ActualizarContrasenaDto;
import com.ecovolt.demo.dtos.ActualizarPerfilUsuarioDto;
import com.ecovolt.demo.dtos.UsuarioDTO;
import com.ecovolt.demo.entities.Rol;
import com.ecovolt.demo.entities.Usuario;
import com.ecovolt.demo.exceptions.BadRequestException;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.RolRepositorio;
import com.ecovolt.demo.repositories.UsuarioRepositorio;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UsuarioService(UsuarioRepositorio usuarioRepositorio,
                          RolRepositorio rolRepositorio,
                          PasswordEncoder passwordEncoder,
                          ModelMapper modelMapper) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public UsuarioDTO create(Usuario request) {
        if (request.getContrasena() != null) {
            request.setContrasena(passwordEncoder.encode(request.getContrasena()));
        }
        Usuario usuario = usuarioRepositorio.save(request);
        UsuarioDTO usuarioDTO = modelMapper.map(usuario, UsuarioDTO.class);
        usuarioDTO.setRoles(usuario.getRoles().stream().map(Rol::getNombre).sorted().toList());
        return usuarioDTO;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        return usuarioRepositorio.findAll()
                .stream()
                .map(usuario -> {
                    UsuarioDTO usuarioDTO = modelMapper.map(usuario, UsuarioDTO.class);
                    usuarioDTO.setRoles(usuario.getRoles().stream().map(Rol::getNombre).sorted().toList());
                    return usuarioDTO;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO findById(Long id) {
        Usuario usuario = findUser(id);
        UsuarioDTO usuarioDTO = modelMapper.map(usuario, UsuarioDTO.class);
        usuarioDTO.setRoles(usuario.getRoles().stream().map(Rol::getNombre).sorted().toList());
        return usuarioDTO;
    }

    @Transactional
    public UsuarioDTO updateProfile(Long id, ActualizarPerfilUsuarioDto request) {
        Usuario usuario = findUser(id);

        modelMapper.map(request, usuario);
        usuario.setNombre(usuario.getNombre().trim());

        usuario = usuarioRepositorio.save(usuario);
        UsuarioDTO usuarioDTO = modelMapper.map(usuario, UsuarioDTO.class);
        usuarioDTO.setRoles(usuario.getRoles().stream().map(Rol::getNombre).sorted().toList());
        return usuarioDTO;
    }

    @Transactional
    public void updatePassword(Long id, ActualizarContrasenaDto request) {
        Usuario usuario = findUser(id);

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            throw new BadRequestException("La contrasena actual no coincide");
        }

        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepositorio.save(usuario);
    }

    @Transactional
    public void delete(Long id) {
        Usuario usuario = findUser(id);
        usuarioRepositorio.delete(usuario);
    }

    @Transactional
    public Rol createRole(Rol request) {
        request.setId(null);
        return rolRepositorio.save(request);
    }

    @Transactional(readOnly = true)
    public List<Rol> findAllRoles() {
        return rolRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Rol findRoleById(Long id) {
        return findRole(id);
    }

    @Transactional
    public Rol updateRole(Long id, Rol request) {
        Rol rol = findRole(id);
        rol.setNombre(request.getNombre());
        return rolRepositorio.save(rol);
    }

    @Transactional
    public void deleteRole(Long id) {
        Rol rol = findRole(id);
        rolRepositorio.delete(rol);
    }

    @Transactional
    public UsuarioDTO updateNotificationSettings(Long id, ConfiguracionNotificacionesDto request) {
        Usuario usuario = findUser(id);

        modelMapper.map(request, usuario);
        usuario.setNotificarConsumoExcesivo(request.getConsumoExcesivo());
        usuario.setNotificarUsoProlongado(request.getUsoProlongado());
        usuario.setNotificarReporteSemanal(request.getReporteSemanal());

        usuario = usuarioRepositorio.save(usuario);
        UsuarioDTO usuarioDTO = modelMapper.map(usuario, UsuarioDTO.class);
        usuarioDTO.setRoles(usuario.getRoles().stream().map(Rol::getNombre).sorted().toList());
        return usuarioDTO;
    }

    private Usuario findUser(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Rol findRole(Long id) {
        return rolRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));
    }

}
