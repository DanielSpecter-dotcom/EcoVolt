package com.ecovolt.demo.Config;

import com.ecovolt.demo.dtos.request.DeviceCreateDto;
import com.ecovolt.demo.dtos.request.NotificationSettingsDto;
import com.ecovolt.demo.dtos.request.RegisterRequestDto;
import com.ecovolt.demo.dtos.response.DeviceResponseDto;
import com.ecovolt.demo.dtos.response.UsuarioResponseDto;
import com.ecovolt.demo.entities.Rol;
import com.ecovolt.demo.entities.Usuario;
import com.ecovolt.demo.entities.DispositivoVirtual;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // Agrupamos los mapeos por entidad para que sea legible
        configureUserMappings(modelMapper);
        configureDeviceMappings(modelMapper);

        return modelMapper;
    }

    private void configureUserMappings(ModelMapper mm) {
        // Registro
        mm.createTypeMap(RegisterRequestDto.class, Usuario.class)
                .addMappings(m -> {
                    m.skip(Usuario::setId);
                    m.map(RegisterRequestDto::getTipoUso, Usuario::setTipoUsuario);
                });

        // Configuración de Notificaciones
        mm.createTypeMap(NotificationSettingsDto.class, Usuario.class)
                .addMappings(m -> {
                    m.map(NotificationSettingsDto::getConsumoExcesivo, Usuario::setNotificarConsumoExcesivo);
                    m.map(NotificationSettingsDto::getUsoProlongado, Usuario::setNotificarUsoProlongado);
                    m.map(NotificationSettingsDto::getReporteSemanal, Usuario::setNotificarReporteSemanal);
                });

        // Respuesta de Usuario (con Converter inline para ahorrar espacio)
        mm.createTypeMap(Usuario.class, UsuarioResponseDto.class)
                .addMappings(m -> m.using(ctx ->
                                ctx.getSource() == null ? List.of() :
                                        ((Set<Rol>) ctx.getSource()).stream()
                                                .map(Rol::getNombre).sorted().toList())
                        .map(Usuario::getRoles, UsuarioResponseDto::setRoles));
    }

    private void configureDeviceMappings(ModelMapper mm) {
        // Creación de Dispositivo
        mm.createTypeMap(DeviceCreateDto.class, DispositivoVirtual.class)
                .addMappings(m -> {
                    m.skip(DispositivoVirtual::setId);
                    m.map(DeviceCreateDto::getTipoDispositivo, DispositivoVirtual::setTipo);
                    m.map(DeviceCreateDto::getPotenciaEstimadaWatts, DispositivoVirtual::setPotenciaWatts);
                });

        // Respuesta de Dispositivo (Mapeo de ID de habitación)
        mm.createTypeMap(DispositivoVirtual.class, DeviceResponseDto.class)
                .addMapping(src -> src.getHabitacion().getId(), DeviceResponseDto::setHabitacionId);
    }
}
