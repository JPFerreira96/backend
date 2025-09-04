// // package com.example.userservice.security;

// // import java.util.List;

// // import org.springframework.context.annotation.Bean;
// // import org.springframework.context.annotation.Configuration;
// // import org.springframework.http.HttpMethod;
// // import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// // import org.springframework.security.config.http.SessionCreationPolicy;
// // import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;   // <-- importa
// // import org.springframework.security.crypto.password.PasswordEncoder;   // <-- importa
// // import org.springframework.security.web.SecurityFilterChain;
// // import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// // import org.springframework.web.cors.*;

// // @Configuration
// // public class WebSecurityConfig {

// //   private final JwtAuthFilter jwtAuthFilter;
// //   public WebSecurityConfig(JwtAuthFilter jwtAuthFilter) { this.jwtAuthFilter = jwtAuthFilter; }

// //   // ⬇⬇⬇ ADICIONE ESTE BEAN
// //   @Bean
// //   public PasswordEncoder passwordEncoder() {
// //     return new BCryptPasswordEncoder();
// //   }
// //   // ⬆⬆⬆

// //   @Bean
// //   SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
// //     http
// //       .csrf(csrf -> csrf.disable())
// //       .cors(c -> c.configurationSource(corsConfigurationSource()))
// //       .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
// //       .authorizeHttpRequests(auth -> auth
// //         .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
// //         .requestMatchers("/auth/**").permitAll()
// //         .requestMatchers("/users/**").hasRole("ADMIN")
// //         .requestMatchers("/cards/**").hasAnyRole("USER","ADMIN")
// //         .anyRequest().authenticated()
// //       )
// //       .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
// //     return http.build();
// //   }

// //   @Bean
// //   CorsConfigurationSource corsConfigurationSource() {
// //     var cfg = new CorsConfiguration();
// //     cfg.setAllowedOrigins(List.of("http://localhost:4200"));
// //     cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
// //     cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
// //     cfg.setAllowCredentials(true);
// //     cfg.setMaxAge(3600L);
// //     var src = new UrlBasedCorsConfigurationSource();
// //     src.registerCorsConfiguration("/**", cfg);
// //     return src;
// //   }
// // }

// // src/main/java/com/example/userservice/security/WebSecurityConfig.java
// // package com.example.userservice.security;

// // import org.springframework.context.annotation.Bean;
// // import org.springframework.context.annotation.Configuration;
// // import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// // import org.springframework.security.web.SecurityFilterChain;

// // @Configuration
// // public class WebSecurityConfig {

// //   @Bean
// //   SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
// //     http
// //       .csrf(csrf -> csrf.disable())
// //       .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // <-- tudo liberado
// //     return http.build();
// //   }


// //   // @Bean
// //   // CorsConfigurationSource corsConfigurationSource() {
// //   //   var cfg = new CorsConfiguration();
// //   //   cfg.setAllowedOrigins(List.of("http://localhost:4200"));
// //   //   cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
// //   //   cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
// //   //   cfg.setAllowCredentials(true);
// //   //   cfg.setMaxAge(3600L);
// //   //   var src = new UrlBasedCorsConfigurationSource();
// //   //   src.registerCorsConfiguration("/**", cfg);
// //   //   return src;
// //   // }
// // }

// // src/main/java/com/example/userservice/security/WebSecurityConfig.java
// package com.example.userservice.security;

// import java.util.List;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.web.cors.*;

// @Configuration
// public class WebSecurityConfig {

//   private final JwtAuthFilter jwtAuthFilter;
//   public WebSecurityConfig(JwtAuthFilter jwtAuthFilter) { this.jwtAuthFilter = jwtAuthFilter; }

//   // (A) mantém o PasswordEncoder
//   @Bean
//   public PasswordEncoder passwordEncoder() {
//     return new BCryptPasswordEncoder();
//   }

//   @Bean
//   SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//     http
//       .csrf(csrf -> csrf.disable())
//       .cors(c -> c.configurationSource(corsConfigurationSource()))
//       .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//       .authorizeHttpRequests(auth -> auth
//         .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//         // (B) libere as DUAS variações de caminho
//         .requestMatchers("/api/auth/**", "/auth/**").permitAll()
//         .requestMatchers("/api/users/**", "/users/**").hasRole("ADMIN")
//         .requestMatchers("/api/cards/**", "/cards/**").hasAnyRole("USER","ADMIN")
//         .anyRequest().authenticated()
//       )
//       // (C) evite BASIC/FORM para não forçar 401/redirect
//       .httpBasic(b -> b.disable())
//       .formLogin(f -> f.disable())
//       .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//     return http.build();
//   }

//   @Bean
//   CorsConfigurationSource corsConfigurationSource() {
//     var cfg = new CorsConfiguration();
//     cfg.setAllowedOrigins(List.of("http://localhost:4200"));
//     cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
//     cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
//     cfg.setAllowCredentials(true);
//     cfg.setMaxAge(3600L);
//     var src = new UrlBasedCorsConfigurationSource();
//     src.registerCorsConfiguration("/**", cfg);
//     return src;
//   }
// }

package com.example.userservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  public WebSecurityConfig(JwtAuthFilter jwtAuthFilter) { this.jwtAuthFilter = jwtAuthFilter; }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/auth/**").permitAll()
        .anyRequest().authenticated()
      )
      .httpBasic(b -> b.disable())
      .formLogin(f -> f.disable())
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}


