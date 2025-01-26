package com.example.festimo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
        //@PropertySource("classpath:properties/env.properties") // env.properties 파일 소스 등록
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:properties/env.properties")// env.properties 파일 소스 등록
})
public class PropertyConfig {
}
