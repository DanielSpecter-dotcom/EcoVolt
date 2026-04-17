package com.ecovolt.demo.Config;


import com.ecovolt.demo.Dto.Request.UsuarioCreateDto;
import com.ecovolt.demo.Dto.Response.UsuarioResponseDto;
import com.ecovolt.demo.Entities.UsuarioEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.createTypeMap(UsuarioEntity.class, UsuarioResponseDto.class);
        modelMapper.createTypeMap(UsuarioCreateDto.class, UsuarioEntity.class);

        return modelMapper;
    }
}
