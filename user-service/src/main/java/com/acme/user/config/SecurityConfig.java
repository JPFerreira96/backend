package com.acme.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.acme.user.security.JwtFilter;



@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain filter(HttpSecurity http, JwtFilter jwt) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> {
        cors.configurationSource(corsConfigurationSource());  // Configura CORS
      })
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .headers(h -> h
        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
        .frameOptions(f -> f.deny())
      )
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/swagger**", "/v3/api-docs/**", "/actuator/health").permitAll()
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/internal/**").permitAll()
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  // Configuração do CORS com as origens permitidas
  private UrlBasedCorsConfigurationSource corsConfigurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOriginPattern("http://localhost:4200"); // Usando addAllowedOriginPattern
    config.addAllowedOriginPattern("http://example.com");    // Usando addAllowedOriginPattern
    config.addAllowedMethod("GET");
    config.addAllowedMethod("POST");
    config.addAllowedMethod("PUT");
    config.addAllowedMethod("DELETE");
    config.addAllowedHeader("*"); // Permite qualquer cabeçalho
    source.registerCorsConfiguration("/**", config); // Aplica as configurações de CORS a todas as URLs
    return source;
  }
}
