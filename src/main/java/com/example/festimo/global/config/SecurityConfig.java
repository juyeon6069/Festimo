package com.example.festimo.global.config;


import com.example.festimo.domain.oauth.service.CustomOAuth2UserService;
import com.example.festimo.global.utils.jwt.CustomOAuth2FailureHandler;
import com.example.festimo.global.utils.jwt.JwtAuthenticationFilter;
import com.example.festimo.global.utils.jwt.JwtTokenProvider;
import com.example.festimo.global.utils.jwt.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

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
                        // 정적 리소스
                        .requestMatchers(
                                "/",
                                "/imgs/**",
                                "/index.html",
                                "/static/**",
                                "/assets/**",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico"
                        ).permitAll()

                        // 프론트엔드 라우팅 경로
                        .requestMatchers(
                                "/community",
                                "/community/**",
                                "/login",
                                "/register",
                                "/html/festival.html",
                                "/html/login.html",
                                "/html/**"
//                                ""     // 화면 확인을 위한 임시 허용
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/register", "/api/login").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/error").permitAll()// 누구나 가능
                        .requestMatchers(HttpMethod.POST, "/api/logout").authenticated()

                        .requestMatchers(HttpMethod.GET, "/manuallyGetAllEvents").permitAll() // 수동으로 축제 api 불러오기 허용
                        .requestMatchers(HttpMethod.GET, "/api/events").permitAll() // 축제 전체 조회 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/{eventId}").permitAll() // 각각의 축제 조회 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/search").permitAll() // 축제 검색 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/filter/month").permitAll() // 축제 필터링 비회원 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/filter/region").permitAll() // 축제 필터링 비회원 허용

                        // 게시글 작성 및 수정 인증
                        .requestMatchers(
                                "/post/write",
                                "/post/edit/**"
                        ).authenticated()

                        // 커뮤니티 관련 공개/인증 API
                        .requestMatchers(HttpMethod.GET, "/api/companions").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/companions").authenticated()
                        .requestMatchers("/api/companions/search/**",
                                "/api/tags/**",
                                "/api/companions/top-weekly").permitAll()
                        .requestMatchers("/api/companions/{postId}/**").authenticated()

                        // Swagger UI
                        .requestMatchers(
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/api-docs/**"
                        ).permitAll()

                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/oauth2/token").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // 권한 기반 접근 제어 관리자만 사용 가능
                        .anyRequest().authenticated()    // 나머지는 로그인한 사용자만

                );

        // Add JWT Authentication Filter
        http.addFilterBefore(
            new JwtAuthenticationFilter(jwtTokenProvider),
            UsernamePasswordAuthenticationFilter.class
        );

        http
            .oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                    .userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(customOAuth2FailureHandler));


        //에러처리
        http.exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            }) // 인증 실패 시 401 반환
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            }) // 권한 부족 시 403 반환
        );



        return http.build();
    }
    // // CORS 설정 추가
    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     CorsConfiguration configuration = new CorsConfiguration();
    //     configuration.addAllowedOrigin("http://localhost:3000"); // 허용할 클라이언트 도메인
    //     configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
    //     configuration.addAllowedHeader("*"); // 모든 헤더 허용
    //     configuration.setAllowCredentials(true); // 쿠키 사용 허용
    //
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", configuration); // 모든 경로에 CORS 설정 적용
    //     return source;
    // }
}
