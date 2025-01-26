package com.example.festimo.global.config;

import com.example.festimo.domain.festival.dto.FestivalTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String Serializer (Key)
        template.setKeySerializer(new StringRedisSerializer());

        // Value Serializer 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // List<FestivalTO> Serializer
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, FestivalTO.class);
        Jackson2JsonRedisSerializer<List<FestivalTO>> listSerializer = new Jackson2JsonRedisSerializer<>(listType);
        listSerializer.setObjectMapper(objectMapper);

        // General Object Serializer
        Jackson2JsonRedisSerializer<Object> objectSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        objectSerializer.setObjectMapper(objectMapper);

        // Value Serializer 설정
        template.setValueSerializer(objectSerializer);

        // Hash에서 사용하는 Serializer
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(objectSerializer);

        return template;
    }
}
