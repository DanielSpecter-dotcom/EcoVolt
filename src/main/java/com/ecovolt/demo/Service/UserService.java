package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.NotificationSettingsDto;
import com.ecovolt.demo.Dto.Request.UpdatePasswordDto;
import com.ecovolt.demo.Dto.Request.UpdateUserProfileDto;
import com.ecovolt.demo.Dto.Response.UsuarioResponseDto;
import com.ecovolt.demo.Entities.UsuarioEntity;
import com.ecovolt.demo.Exception.BadRequestException;
import com.ecovolt.demo.Exception.ResourceNotFoundException;
import com.ecovolt.demo.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Transactional
    public UsuarioResponseDto updateProfile(Long id, UpdateUserProfileDto request) {
        UsuarioEntity usuario = findUser(id);

        boolean hasNombre = request.getNombre() != null && !request.getNombre().trim().isEmpty();
        boolean hasFoto = request.getFotoPerfilUrl() != null && !request.getFotoPerfilUrl().trim().isEmpty();

        if (!hasNombre && !hasFoto) {
            throw new BadRequestException("Debe enviar al menos el nombre o la foto de perfil");
        }

        if (hasNombre) {
            usuario.setNombre(request.getNombre().trim());
        }

        if (hasFoto) {
            usuario.setFotoPerfilUrl(request.getFotoPerfilUrl().trim());
        }

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
        modelMapper.map(usuario, response);
        return response;
    }
}
