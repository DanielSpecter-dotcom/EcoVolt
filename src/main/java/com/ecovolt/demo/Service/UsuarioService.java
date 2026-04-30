package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.NotificationSettingsDto;
import com.ecovolt.demo.Dto.Request.UpdatePasswordDto;
import com.ecovolt.demo.Dto.Request.UpdateUserProfileDto;
import com.ecovolt.demo.Dto.Response.UsuarioResponseDto;
import com.ecovolt.demo.Entities.RolEntity;
import com.ecovolt.demo.Entities.UsuarioEntity;
import com.ecovolt.demo.Exception.BadRequestException;
import com.ecovolt.demo.Exception.ResourceNotFoundException;
import com.ecovolt.demo.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDto updateProfile(Long id, UpdateUserProfileDto request) {
        UsuarioEntity usuario = findUser(id);

        usuario.setNombre(request.getNombre().trim());

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void updatePassword(Long id, UpdatePasswordDto request) {
        UsuarioEntity usuario = findUser(id);

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            throw new BadRequestException("La contrasena actual no coincide");
        }

        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDto updateNotificationSettings(Long id, NotificationSettingsDto request) {
        UsuarioEntity usuario = findUser(id);

        usuario.setNotificarConsumoExcesivo(request.getConsumoExcesivo());
        usuario.setNotificarUsoProlongado(request.getUsoProlongado());
        usuario.setNotificarReporteSemanal(request.getReporteSemanal());

        return toResponse(usuarioRepository.save(usuario));
    }

    private UsuarioEntity findUser(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private UsuarioResponseDto toResponse(UsuarioEntity usuario) {
        UsuarioResponseDto response = new UsuarioResponseDto();
        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setUsername(usuario.getUsername());
        response.setCorreo(usuario.getCorreo());
        response.setTipoUsuario(usuario.getTipoUsuario());
        response.setActivo(usuario.isActivo());
        response.setNotificarConsumoExcesivo(usuario.isNotificarConsumoExcesivo());
        response.setNotificarUsoProlongado(usuario.isNotificarUsoProlongado());
        response.setNotificarReporteSemanal(usuario.isNotificarReporteSemanal());
        response.setRoles(mapRoleNames(usuario));
        return response;
    }

    private List<String> mapRoleNames(UsuarioEntity usuario) {
        return usuario.getRoles()
                .stream()
                .map(RolEntity::getNombre)
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}
