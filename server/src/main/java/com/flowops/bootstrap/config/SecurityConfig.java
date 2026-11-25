package com.flowops.bootstrap.config;

import com.flowops.shared.infrastructure.JwtAuthenticationFilter;
import com.flowops.shared.infrastructure.AuthOriginCsrfFilter;
import com.flowops.shared.infrastructure.SecurityErrorWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.http.server.ServerHttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowops.shared.infrastructure.ErrorResponseDto;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthOriginCsrfFilter authOriginCsrfFilter,
                          SecurityErrorWriter securityErrorWriter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authOriginCsrfFilter = authOriginCsrfFilter;
        this.securityErrorWriter = securityErrorWriter;
    }


    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthOriginCsrfFilter authOriginCsrfFilter;
    private final SecurityErrorWriter securityErrorWriter;

    @Value("${flowops.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${flowops.auth.bcrypt-strength:12}")
    private int bcryptStrength;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) -> securityErrorWriter.write(response, 401,
                                "UNAUTHORIZED", "Xác thực không thành công hoặc phiên đã hết hạn"))
                        .accessDeniedHandler((request, response, ex) -> securityErrorWriter.write(response, 403,
                                "ACCESS_DENIED", "Bạn không có quyền thực hiện hành động này")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Role-specific endpoints
                        .requestMatchers("/api/v1/workflows/available").hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/workflows/**").authenticated()
                        .requestMatchers("/api/v1/workflows/**", "/api/v1/workflow-versions/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/users/**", "/api/v1/departments/**", "/api/v1/approval-groups/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/dashboard/**", "/api/v1/audit-logs/**", "/api/v1/incidents/**").hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authOriginCsrfFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key", "X-Requested-With", "X-FlowOps-CSRF"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public CorsFilter corsFilter(@Qualifier("corsConfigurationSource") CorsConfigurationSource source, ObjectMapper mapper) {
        CorsFilter filter = new CorsFilter(source);
        filter.setCorsProcessor(new DefaultCorsProcessor() {
            @Override
            protected void rejectRequest(ServerHttpResponse response) throws IOException {
                response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                mapper.writeValue(response.getBody(), ErrorResponseDto.builder().code("ACCESS_DENIED")
                        .message("Origin không được phép").traceId(UUID.randomUUID().toString()).build());
            }
        });
        return filter;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsFilter filter) {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<AuthOriginCsrfFilter> authOriginFilterRegistration(AuthOriginCsrfFilter filter) {
        FilterRegistrationBean<AuthOriginCsrfFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }
}
