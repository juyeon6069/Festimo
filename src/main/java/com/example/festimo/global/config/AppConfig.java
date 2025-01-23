package com.example.festimo.global.config;

import com.example.festimo.domain.post.dto.PostDetailResponse;
import com.example.festimo.domain.post.entity.Post;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // Post -> PostDetailResponse
        modelMapper.createTypeMap(Post.class, PostDetailResponse.class)
                .addMapping(Post::getCreatedAt, PostDetailResponse::setCreatedAt)
                .addMapping(Post::getCategory, PostDetailResponse::setCategory)
                .addMapping(Post::getViews, PostDetailResponse::setViews)
                .addMapping(src -> src.getUser().getNickname(), PostDetailResponse::setNickname)
                .addMappings(mapper -> mapper.skip(PostDetailResponse::setLiked));  // likedByUsers -> isLiked 매핑 스킵

        return modelMapper;
    }
}