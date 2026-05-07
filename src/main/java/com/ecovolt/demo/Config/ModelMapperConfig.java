package com.ecovolt.demo.Config;

import com.ecovolt.demo.dtos.request.CrearDispositivoDto;
import com.ecovolt.demo.dtos.request.ConfiguracionNotificacionesDto;
import com.ecovolt.demo.dtos.request.RegistroUsuarioDto;
import com.ecovolt.demo.dtos.response.DispositivoRespuestaDto;
import com.ecovolt.demo.dtos.response.UsuarioRespuestaDto;
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
        mm.createTypeMap(RegistroUsuarioDto.class, Usuario.class)
                .addMappings(m -> {
                    m.skip(Usuario::setId);
                    m.map(RegistroUsuarioDto::getTipoUso, Usuario::setTipoUsuario);
                });

        // Configuración de Notificaciones
        mm.createTypeMap(ConfiguracionNotificacionesDto.class, Usuario.class)
                .addMappings(m -> {
                    m.map(ConfiguracionNotificacionesDto::getConsumoExcesivo, Usuario::setNotificarConsumoExcesivo);
                    m.map(ConfiguracionNotificacionesDto::getUsoProlongado, Usuario::setNotificarUsoProlongado);
                    m.map(ConfiguracionNotificacionesDto::getReporteSemanal, Usuario::setNotificarReporteSemanal);
                });

        // Respuesta de Usuario (con Converter inline para ahorrar espacio)
        mm.createTypeMap(Usuario.class, UsuarioRespuestaDto.class)
                .addMappings(m -> m.using(ctx ->
                                ctx.getSource() == null ? List.of() :
                                        ((Set<Rol>) ctx.getSource()).stream()
                                                .map(Rol::getNombre).sorted().toList())
                        .map(Usuario::getRoles, UsuarioRespuestaDto::setRoles));
    }

    private void configureDeviceMappings(ModelMapper mm) {
        // Creación de Dispositivo
        mm.createTypeMap(CrearDispositivoDto.class, DispositivoVirtual.class)
                .addMappings(m -> {
                    m.skip(DispositivoVirtual::setId);
                    m.map(CrearDispositivoDto::getTipoDispositivo, DispositivoVirtual::setTipo);
                    m.map(CrearDispositivoDto::getPotenciaEstimadaWatts, DispositivoVirtual::setPotenciaWatts);
                });

        // Respuesta de Dispositivo (Mapeo de ID de habitación)
        mm.createTypeMap(DispositivoVirtual.class, DispositivoRespuestaDto.class)
                .addMapping(src -> src.getHabitacion().getId(), DispositivoRespuestaDto::setHabitacionId);
    }
}
