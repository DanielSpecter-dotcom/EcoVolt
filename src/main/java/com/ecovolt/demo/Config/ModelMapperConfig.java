package com.ecovolt.demo.Config;

import com.ecovolt.demo.Dto.Request.DeviceCreateDto;
import com.ecovolt.demo.Dto.Request.NotificationSettingsDto;
import com.ecovolt.demo.Dto.Request.RegisterRequestDto;
import com.ecovolt.demo.Dto.Response.DeviceResponseDto;
import com.ecovolt.demo.Dto.Response.UsuarioResponseDto;
import com.ecovolt.demo.Entities.RolEntity;
import com.ecovolt.demo.Entities.UsuarioEntity;
import com.ecovolt.demo.Entities.VirtualDeviceEntity;
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

        TypeMap<RegisterRequestDto, UsuarioEntity> registerRequestMap =
                modelMapper.createTypeMap(RegisterRequestDto.class, UsuarioEntity.class);
        registerRequestMap.addMappings(mapper -> {
            mapper.skip(UsuarioEntity::setId);
            mapper.map(RegisterRequestDto::getTipoUso, UsuarioEntity::setTipoUsuario);
        });

        TypeMap<DeviceCreateDto, VirtualDeviceEntity> deviceCreateMap =
                modelMapper.createTypeMap(DeviceCreateDto.class, VirtualDeviceEntity.class);
        deviceCreateMap.addMappings(mapper -> {
            mapper.skip(VirtualDeviceEntity::setId);
            mapper.map(DeviceCreateDto::getTipoDispositivo, VirtualDeviceEntity::setTipo);
            mapper.map(DeviceCreateDto::getPotenciaEstimadaWatts, VirtualDeviceEntity::setPotenciaWatts);
        });

        Converter<Set<RolEntity>, List<String>> roleNamesConverter = context -> {
            if (context.getSource() == null) {
                return List.of();
            }

            return context.getSource()
                    .stream()
                    .map(RolEntity::getNombre)
                    .sorted(Comparator.naturalOrder())
                    .toList();
        };

        TypeMap<UsuarioEntity, UsuarioResponseDto> userResponseMap =
                modelMapper.createTypeMap(UsuarioEntity.class, UsuarioResponseDto.class);
        userResponseMap.addMappings(mapper -> mapper
                .using(roleNamesConverter)
                .map(UsuarioEntity::getRoles, UsuarioResponseDto::setRoles));

        TypeMap<VirtualDeviceEntity, DeviceResponseDto> deviceResponseMap =
                modelMapper.createTypeMap(VirtualDeviceEntity.class, DeviceResponseDto.class);
        deviceResponseMap.addMappings(mapper -> mapper
                .map(source -> source.getHabitacion().getId(), DeviceResponseDto::setHabitacionId));

        TypeMap<NotificationSettingsDto, UsuarioEntity> notificationSettingsMap =
                modelMapper.createTypeMap(NotificationSettingsDto.class, UsuarioEntity.class);
        notificationSettingsMap.addMappings(mapper -> {
            mapper.map(NotificationSettingsDto::getConsumoExcesivo, UsuarioEntity::setNotificarConsumoExcesivo);
            mapper.map(NotificationSettingsDto::getUsoProlongado, UsuarioEntity::setNotificarUsoProlongado);
            mapper.map(NotificationSettingsDto::getReporteSemanal, UsuarioEntity::setNotificarReporteSemanal);
        });

        return modelMapper;
    }
}
