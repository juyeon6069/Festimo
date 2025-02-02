package com.example.festimo.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.TimeUnit

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

        // static 리소스 매핑
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))

        // CSS 리소스 캐싱 비활성화
        registry.addResourceHandler("/**/*.css")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.noCache())
            .resourceChain(false)

        // HTML 파일 접근
        registry.addResourceHandler("/html/**")
            .addResourceLocations("classpath:/static/html/")
            .setCacheControl(CacheControl.noCache())

        // JavaScript 파일의 MIME 타입 문제 해결
        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/")
            .setCacheControl(CacheControl.noCache())

        // 이미지 업로드
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:${System.getProperty("user.dir")}/uploads/posts/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .resourceChain(true)
    }

    // CORS 설정
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .exposedHeaders("Authorization")
            .maxAge(3600)
    }
}