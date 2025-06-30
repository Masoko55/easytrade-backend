package com.example.easytrade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer; // Make sure this import is present
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections; // Import for Collections.singletonList if preferred for single origin

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) // Enable CORS using the corsConfigurationSource Bean
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Allow auth endpoints
                .requestMatchers("/api/users/**").permitAll() // Allow users endpoints (adjust if needed for security)
                .requestMatchers("/api/products/**").permitAll() // Allow product endpoints (adjust later if some need auth)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // For Swagger/OpenAPI
                .anyRequest().authenticated() // All other requests need authentication (if you implement token auth)
                                              // If you don't have token auth yet, you might permitAll for now
                                              // or change this once auth is fully working with tokens.
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Good for REST APIs
            );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // This is your Next.js frontend development server URL
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
        // Allow common methods including OPTIONS for preflight requests
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Allow all headers for simplicity in development, be more specific for production
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        // You can also set maxAge for preflight responses if needed
        // configuration.setMaxAge(3600L); // 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS configuration to all paths under /api/
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}