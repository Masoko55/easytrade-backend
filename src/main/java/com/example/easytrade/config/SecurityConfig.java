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
import java.util.List; // Import List instead of Collections

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
            .cors(Customizer.withDefaults()) // Enable CORS using the corsConfigurationSource Bean below
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless APIs
            .authorizeHttpRequests(auth -> auth
                // These endpoints are public and do not require authentication
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/**").permitAll() // Adjust later for security if needed
                .requestMatchers("/api/products/**").permitAll() // Adjust later for security if needed
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Any other request that is not matched above will require authentication.
                // For now, since we don't have token validation configured yet, you could temporarily
                // change .authenticated() to .permitAll() if you need to test other endpoints without auth.
                // But for a real app, .authenticated() is correct.
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // We are not using server sessions
            );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // --- THIS IS THE UPDATED PART ---
        // List of allowed origins (your frontend URLs)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",                // For your local Next.js development
                "https://easytrade-ui.vercel.app",      // Your Vercel production domain (you might need to adjust this)
                "https://easytrade-ui-*.vercel.app"     // Allows preview deployments from Vercel (e.g., for branches)
        ));
        // --- END OF UPDATED PART ---

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allowed HTTP headers
        // Be more specific in production, but "*" is fine for now.
        // "Authorization" and "Content-Type" are common important ones.
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        
        // Allow credentials (like cookies or Authorization headers) to be sent
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS configuration to all API paths
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}