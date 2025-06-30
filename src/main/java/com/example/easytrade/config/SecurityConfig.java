package com.example.easytrade.config;

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
            // Apply CORS configuration first
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Disable CSRF as we are using a stateless API
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // This rule for OPTIONS requests is still good to have explicitly
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Your public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // All other requests should be authenticated
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // --- CRITICAL PART: VERIFY YOUR VERCEL URLS ---
        // Let's get the exact URLs from your Vercel deployment screenshot.
        // The screenshot showed:
        // 1. easytrade-ui-git-main-ntlhari-ndlovhu-s-projects.vercel.app
        // 2. easytrade-4mxrzbvf1-ntlhari-ndlovhu-s-projects.vercel.app
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://easytrade-ui-git-main-ntlhari-ndlovhu-s-projects.vercel.app",
                "https://easytrade-4mxrzbvf1-ntlhari-ndlovhu-s-projects.vercel.app"
                // You can add your main project domain later, e.g., "https://easytrade-ui.vercel.app"
        ));
        // --- END OF CRITICAL PART ---

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to ALL paths
        
        return source;
    }
}