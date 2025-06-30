package com.example.easytrade.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

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
            // We will rely on the FilterRegistrationBean for CORS, but keeping this
            // can help ensure it's integrated into the HttpSecurity context.
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // This rule for OPTIONS is a good failsafe, but the CorsFilter should handle it first.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    // --- THIS IS THE NEW, HIGH-PRECEDENCE CORS FILTER CONFIGURATION ---
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Add your Vercel frontend domains here.
        // It's critical that these match exactly what the browser's "Origin" header sends.
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://easytrade-ui.vercel.app",
                "https://easytrade-ui-git-main-ntlhari-ndlovhu-s-projects.vercel.app",
                "https://easytrade-4mxrzbvf1-ntlhari-ndlovhu-s-projects.vercel.app",
                "https://easytrade-cosqg2576-ntlhari-ndlovhu-s-projects.vercel.app" // From a previous Vercel screenshot
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        
        source.registerCorsConfiguration("/**", config); // Apply to all paths

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        // Set the order to the highest precedence to ensure it runs before Spring Security's filters
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
    // --- END OF NEW CONFIGURATION ---

    // The old CorsConfigurationSource bean is no longer needed as it's incorporated above.
    // @Bean
    // CorsConfigurationSource corsConfigurationSource() { ... }
}