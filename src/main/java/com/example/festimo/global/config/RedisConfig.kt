package com.example.festimo.global.config

import com.example.festimo.domain.festival.dto.FestivalTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
open class RedisConfig {
    @Bean
    open fun redisTemplate(connectionFactory: RedisConnectionFactory?): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory

        // String Serializer (Key)
        template.keySerializer = StringRedisSerializer()

        // Value Serializer 설정
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        // List<FestivalTO> Serializer
        val listType: JavaType = objectMapper.typeFactory.constructCollectionType(
            MutableList::class.java,
            FestivalTO::class.java
        )
        val listSerializer = Jackson2JsonRedisSerializer<List<FestivalTO>>(listType)
        listSerializer.setObjectMapper(objectMapper)

        // General Object Serializer
        val objectSerializer = Jackson2JsonRedisSerializer(Any::class.java)
        objectSerializer.setObjectMapper(objectMapper)

        // Value Serializer 설정
        template.valueSerializer = objectSerializer

        // Hash에서 사용하는 Serializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = objectSerializer

        return template
    }
}
