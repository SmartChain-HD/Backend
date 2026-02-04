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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 공개 경로
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // GUEST 허용 경로 (권한 요청)
                        .requestMatchers("/api/v1/roles/**").permitAll()
                        // 보호 대상: GUEST 이외 역할만 접근 가능
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
                        // Admin Chat API는 REVIEWER만 접근 가능
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
