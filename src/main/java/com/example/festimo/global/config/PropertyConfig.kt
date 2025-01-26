package com.example.festimo.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources

@Configuration
@PropertySources(
    PropertySource("classpath:application.properties"),
    PropertySource("classpath:properties/env.properties")
)
open class PropertyConfig
