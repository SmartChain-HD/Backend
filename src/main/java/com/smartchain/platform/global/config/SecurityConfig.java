package com.smartchain.platform.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.global.error.ErrorCode;
import com.smartchain.platform.global.response.ErrorResponse;
import com.smartchain.platform.global.security.JwtAuthenticationFilter;
import com.smartchain.platform.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    // 시큐리티의 '검문' 자체를 무시할 정적 자원/헬스체크 경로 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/health", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        
                        // 비로그인 사용자도 접근 가능한 API 경로
                        .requestMatchers("/api/v1/auth/**", "/api/v1/roles/**").permitAll()

                        // 로그인 및 공통 역할이 필요한 경로
                        .requestMatchers(
                                "/api/v1/diagnostics/**",
                                "/api/v1/approvals/**",
                                "/api/v1/reviews/**",
                                "/api/v1/notifications/**",
                                "/api/v1/files/**",
                                "/api/v1/ai/**",
                                "/api/v1/campaigns/**",
                                "/api/v1/domains/**",
                                "/api/v1/management/**",
                                "/api/v1/jobs/**",
                                "/api/v1/chat/**"
                        ).hasAnyRole("DRAFTER", "APPROVER", "REVIEWER")

                        // REVIEWER 전용 API (충돌 지점 해결)
                        .requestMatchers("/api/v1/risk/**").hasRole("REVIEWER")
                        .requestMatchers("/api/v1/admin/chat/**").hasRole("REVIEWER")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    objectMapper.writeValueAsString(ErrorResponse.of(ErrorCode.INVALID_TOKEN))
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    objectMapper.writeValueAsString(ErrorResponse.of(ErrorCode.ACCESS_DENIED))
                            );
                        })
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}