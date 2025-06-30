package com.example.easytrade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
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
import java.util.List;

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
            // Use the 'corsConfigurationSource' bean defined below
            .cors(Customizer.withDefaults())
            // Disable CSRF for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)
            // Permit ALL requests to ALL endpoints. This simplifies things for a school project.
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Configure session management to be stateless (important for APIs)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow requests from any origin.
        configuration.setAllowedOrigins(List.of("*"));
        
        // Allow all standard HTTP methods.
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers.
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // IMPORTANT: Even with wildcard origins, we can explicitly allow credentials.
        // While some browser/server combos are strict, this often works and is needed for your auth flow.
        configuration.setAllowCredentials(true); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS configuration to all paths on the server.
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}