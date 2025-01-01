package com.example.festimo.global.config;

import com.example.festimo.domain.user.service.NaverOauth2UserService;
import com.example.festimo.global.utils.jwt.JwtAuthenticationFilter;
import com.example.festimo.global.utils.jwt.JwtTokenProvider;
import com.example.festimo.global.utils.jwt.NaverLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final NaverOauth2UserService naverOauth2UserService;
    private final NaverLoginSuccessHandler naverLoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/register", "/api/login").permitAll()
                        .requestMatchers(
                                "/",
                                "/imgs/**",
                                "/index.html",
                                "/community",
                                "/community/**",
                                "/login",
                                "/register",
                                "/static/**",
                                "/assets/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/error").permitAll()// 누구나 가능 , "/oauth2/**"
                        .requestMatchers(HttpMethod.GET, "/api/events").permitAll() // 축제 전체 조회 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/{eventId}").permitAll() // 각각의 축제 조회 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/search").permitAll() // 축제 검색 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/filter/month").permitAll() // 축제 필터링 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/filter/region").permitAll() // 축제 필터링 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events").permitAll() // 축제 전체 조회 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/{eventId}").permitAll() // 각각의 축제 조회 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/search").permitAll() // 축제 검색 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/filter/month").permitAll() // 축제 필터링 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/filter/region").permitAll() // 축제 필터링 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/companions").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/companions").authenticated()
                        .requestMatchers("/api/tags/popular").permitAll()
                        .requestMatchers("/api/companions/top-weekly").permitAll()
                        .requestMatchers(
                                "/api/companions/{postId}/comments/**",  // 댓글 관련 모든 요청
                                "/api/companions/{postId}/**"            // 게시글 상세 관련 모든 요청
                        ).authenticated()
                        .requestMatchers(
                                "/",
                                "/community/**",
                                "/static/**"
                        ).permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // 권한 기반 접근 제어 관리자만 사용 가능
                        .anyRequest().authenticated()    // 나머지는 로그인한 사용자만
                );
//                .oauth2Login()
//                .defaultSuccessUrl("/api/oauth2/success");

        // Add JWT Authentication Filter
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );
        http
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(naverOauth2UserService))
                        .successHandler(naverLoginSuccessHandler));

        return http.build();
    }
}