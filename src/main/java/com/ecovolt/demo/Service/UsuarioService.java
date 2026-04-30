package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.NotificationSettingsDto;
import com.ecovolt.demo.Dto.Request.UpdatePasswordDto;
import com.ecovolt.demo.Dto.Request.UpdateUserProfileDto;
import com.ecovolt.demo.Dto.Request.UsuarioCreateDto;
import com.ecovolt.demo.Dto.Response.ReniecResponse;
import com.ecovolt.demo.Dto.Response.UsuarioResponseDto;
import com.ecovolt.demo.Entities.UsuarioEntity;
import com.ecovolt.demo.Exception.BadRequestException;
import com.ecovolt.demo.Exception.ResourceNotFoundException;
import com.ecovolt.demo.Repository.UsuarioRepository;
import com.ecovolt.demo.Service.FeingService.ReniecClient;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    private final ReniecClient reniecClient;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${api.token}")
    private String apiToken;

    @Transactional
    public UsuarioResponseDto saveUsuario(UsuarioCreateDto usuarioDto) {
        // 1. Validar formato del DNI
        String dni = usuarioDto.getDni();
        if (dni == null || dni.length() != 8 || !dni.matches("^\\d+$")) {
            throw new IllegalArgumentException("El DNI debe tener 8 dígitos numéricos");
        }

        // 2. Consultar a la API externa de Reniec
        ReniecResponse response = null;
        try {
            // Nota: Verifica si tu API requiere que concatenes "Bearer " antes del token.
            // Si es así, sería: getPersonaInfo(dni, "Bearer " + apiToken)
            response = reniecClient.getPersonaInfo(dni, apiToken);
        } catch (Exception ex) {
            throw new RuntimeException("Error al consultar el servicio de Reniec", ex);
        }

        if (response == null || response.getFirstName() == null) {
            throw new RuntimeException("No se encontró información para el DNI brindado");
        }

        // 3. Mapear datos del DTO a la Entidad
        UsuarioEntity usuarioEntity = new UsuarioEntity();
        modelMapper.map(usuarioDto, usuarioEntity);

        //Mapear username
        String username = response
                .getFirstName()
                .split("\\s+")[0]
                .toLowerCase() + "." +
                response.getFirstLastName().toLowerCase();

        // 4. Sobrescribir los nombres y apellidos reales obtenidos de Reniec
        // Ojo: En tu entidad demo los campos están en singular (nombre, apellido)
        usuarioEntity.setNombre(response.getFirstName());
        usuarioEntity.setApellido(response.getFirstLastName() + " " + response.getSecondLastName());
        usuarioEntity.setUsername(username);

        // 5. Guardar en Base de Datos
        UsuarioEntity usuarioGuardado = usuarioRepository.save(usuarioEntity);

        // 6. Mapear la Entidad guardada al DTO de Respuesta
        UsuarioResponseDto usuarioResponseDto = new UsuarioResponseDto();
        modelMapper.map(usuarioGuardado, usuarioResponseDto);
        return usuarioResponseDto;
    }

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
        modelMapper.map(usuario, response);
        return response;
    }

}
