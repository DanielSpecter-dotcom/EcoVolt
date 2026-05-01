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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Transactional
    public UsuarioResponseDto updateProfile(Long id, UpdateUserProfileDto request) {
        UsuarioEntity usuario = findUser(id);

        modelMapper.map(request, usuario);
        usuario.setNombre(usuario.getNombre().trim());

        return modelMapper.map(usuarioRepository.save(usuario), UsuarioResponseDto.class);
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

        modelMapper.map(request, usuario);

        return modelMapper.map(usuarioRepository.save(usuario), UsuarioResponseDto.class);
    }

    private UsuarioEntity findUser(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

}
