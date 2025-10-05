// package com.acme.card.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import com.acme.card.security.JwtFilter;



// @Configuration
// @EnableMethodSecurity
// public class SecurityConfig {

//   @Bean
//   SecurityFilterChain filter(HttpSecurity http, JwtFilter jwt) throws Exception {
//     http
//       .csrf(csrf -> csrf.disable())
//       .cors(cors -> {
//         cors.configurationSource(corsConfigurationSource());  // Configura CORS
//       })
//       .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//       .headers(h -> h
//         .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
//         .frameOptions(f -> f.deny())
//       )
//       .authorizeHttpRequests(auth -> auth
//         .requestMatchers("/swagger**", "/v3/api-docs/**", "/actuator/health").permitAll()
//         .anyRequest().authenticated()
//       )
//       .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);

//     return http.build();
//   }

//   // Configuração do CORS com as origens permitidas
//   private UrlBasedCorsConfigurationSource corsConfigurationSource() {
//     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//     CorsConfiguration config = new CorsConfiguration();
//     config.addAllowedOriginPattern("http://localhost:4200"); // Usando addAllowedOriginPattern
//     config.addAllowedOriginPattern("http://localhost:4201"); // Porta alternativa Angular
//     config.addAllowedOriginPattern("http://example.com");    // Usando addAllowedOriginPattern
//     config.addAllowedMethod("GET");
//     config.addAllowedMethod("POST");
//     config.addAllowedMethod("PUT");
//     config.addAllowedMethod("DELETE");
//     config.addAllowedHeader("*"); // Permite qualquer cabeçalho
//     source.registerCorsConfiguration("/**", config); // Aplica as configurações de CORS a todas as URLs
//     return source;
//   }
// }


package com.acme.card.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.acme.card.security.JwtFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain filter(HttpSecurity http, JwtFilter jwt) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .headers(h -> h
        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
        .frameOptions(f -> f.deny())
      )
      .authorizeHttpRequests(auth -> auth
        // libera swagger/health
        .requestMatchers("/swagger**", "/v3/api-docs/**", "/actuator/health").permitAll()
        // *** libera canal interno entre serviços (sem JWT) ***
        .requestMatchers("/internal/**").permitAll()
        // o resto exige JWT
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    var config = new CorsConfiguration();
    // origens de dev
    config.addAllowedOriginPattern("http://localhost:4200");
    config.addAllowedOriginPattern("http://localhost:4201");
    config.addAllowedOriginPattern("http://example.com");

    // métodos/headers
  config.addAllowedMethod("GET");
  config.addAllowedMethod("POST");
  config.addAllowedMethod("PUT");
  config.addAllowedMethod("DELETE");
  config.addAllowedMethod("PATCH");
    config.addAllowedHeader("*");
    // se a UI for chamar internos via browser algum dia:
    config.addAllowedHeader("X-Internal-Secret");
    config.addAllowedHeader("X-User-Id");

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
