package com.ecovolt.demo.Service;

import com.ecovolt.demo.Config.ModelMapperConfig;
import com.ecovolt.demo.Dto.Request.UsuarioCreateDto;
import com.ecovolt.demo.Dto.Response.ReniecResponse;
import com.ecovolt.demo.Dto.Response.UsuarioResponseDto;
import com.ecovolt.demo.Entities.UsuarioEntity;
import com.ecovolt.demo.Repository.UsuarioRepository;
import com.ecovolt.demo.Service.FeingService.ReniecClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ReniecClient reniecClient;
    @Value("${api.token}")
    private String apiToken;

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

}
