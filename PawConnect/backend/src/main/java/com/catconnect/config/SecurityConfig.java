package com.catconnect.config;

import com.catconnect.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/api/auth/**").permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/lost-found", "/api/adoptions",
                                "/api/moments", "/api/memes", "/api/upload", "/api/donations/{id}/contribute",
                                "/api/donations", "/api/moments/{id}/comments", "/api/moments/{id}/share").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/memes/{id}/like", "/api/vets/{id}/react", "/api/shops/{id}/react", "/api/shelters/{id}/react", "/api/memes/{id}/react", "/api/adoptions/{id}/react", "/api/lost-found/{id}/react", "/api/moments/{id}/react", "/api/donations/{id}/react").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/lost-found/*", "/api/moments/*", "/api/memes/*",
                                "/api/adoptions/*", "/api/donations/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/lost-found/*", "/api/moments/*",
                                "/api/memes/*", "/api/adoptions/*", "/api/donations/*").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
