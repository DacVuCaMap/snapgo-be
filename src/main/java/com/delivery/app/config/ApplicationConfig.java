package com.delivery.app.config;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class ApplicationConfig {
    @Bean
    public ModelMapper modelMapper(){return new ModelMapper();}
}
