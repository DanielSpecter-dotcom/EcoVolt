package com.ecovolt.demo.config;

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

        TypeMap<RegisterRequestDto, Usuario> registerRequestMap =
                modelMapper.createTypeMap(RegisterRequestDto.class, Usuario.class);
        registerRequestMap.addMappings(mapper -> {
            mapper.skip(Usuario::setId);
            mapper.map(RegisterRequestDto::getTipoUso, Usuario::setTipoUsuario);
        });

        TypeMap<DeviceCreateDto, DispositivoVirtual> deviceCreateMap =
                modelMapper.createTypeMap(DeviceCreateDto.class, DispositivoVirtual.class);
        deviceCreateMap.addMappings(mapper -> {
            mapper.skip(DispositivoVirtual::setId);
            mapper.map(DeviceCreateDto::getTipoDispositivo, DispositivoVirtual::setTipo);
            mapper.map(DeviceCreateDto::getPotenciaEstimadaWatts, DispositivoVirtual::setPotenciaWatts);
        });

        Converter<Set<Rol>, List<String>> roleNamesConverter = context -> {
            if (context.getSource() == null) {
                return List.of();
            }

            return context.getSource()
                    .stream()
                    .map(Rol::getNombre)
                    .sorted(Comparator.naturalOrder())
                    .toList();
        };

        TypeMap<Usuario, UsuarioResponseDto> userResponseMap =
                modelMapper.createTypeMap(Usuario.class, UsuarioResponseDto.class);
        userResponseMap.addMappings(mapper -> mapper
                .using(roleNamesConverter)
                .map(Usuario::getRoles, UsuarioResponseDto::setRoles));

        TypeMap<DispositivoVirtual, DeviceResponseDto> deviceResponseMap =
                modelMapper.createTypeMap(DispositivoVirtual.class, DeviceResponseDto.class);
        deviceResponseMap.addMappings(mapper -> mapper
                .map(source -> source.getHabitacion().getId(), DeviceResponseDto::setHabitacionId));

        TypeMap<NotificationSettingsDto, Usuario> notificationSettingsMap =
                modelMapper.createTypeMap(NotificationSettingsDto.class, Usuario.class);
        notificationSettingsMap.addMappings(mapper -> {
            mapper.map(NotificationSettingsDto::getConsumoExcesivo, Usuario::setNotificarConsumoExcesivo);
            mapper.map(NotificationSettingsDto::getUsoProlongado, Usuario::setNotificarUsoProlongado);
            mapper.map(NotificationSettingsDto::getReporteSemanal, Usuario::setNotificarReporteSemanal);
        });

        return modelMapper;
    }
}
