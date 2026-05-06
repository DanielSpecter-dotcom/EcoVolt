package com.ecovolt.demo.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.NotificationSettingsDto;
import com.ecovolt.demo.dtos.request.UpdatePasswordDto;
import com.ecovolt.demo.dtos.request.UpdateUserProfileDto;
import com.ecovolt.demo.dtos.response.UsuarioResponseDto;
import com.ecovolt.demo.entities.Usuario;
import com.ecovolt.demo.exceptions.BadRequestException;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.UsuarioRepositorio;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public UsuarioResponseDto updateProfile(Long id, UpdateUserProfileDto request) {
        Usuario usuario = findUser(id);

        modelMapper.map(request, usuario);
        usuario.setNombre(usuario.getNombre().trim());

        return modelMapper.map(usuarioRepositorio.save(usuario), UsuarioResponseDto.class);
    }

    @Transactional
    public void updatePassword(Long id, UpdatePasswordDto request) {
        Usuario usuario = findUser(id);

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            throw new BadRequestException("La contrasena actual no coincide");
        }

        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepositorio.save(usuario);
    }

    @Transactional
    public UsuarioResponseDto updateNotificationSettings(Long id, NotificationSettingsDto request) {
        Usuario usuario = findUser(id);

        modelMapper.map(request, usuario);

        return modelMapper.map(usuarioRepositorio.save(usuario), UsuarioResponseDto.class);
    }

    private Usuario findUser(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

}
