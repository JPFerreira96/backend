// // backend/gateway/src/main/java/com/example/gateway/GatewayApplication.java
// package com.example.gateway;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cloud.gateway.route.RouteLocator;
// import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
// import org.springframework.context.annotation.Bean;

// @SpringBootApplication
// public class GatewayApplication {

//   public static void main(String[] args) {
//     SpringApplication.run(GatewayApplication.class, args);
//   }

//   @Bean
//   RouteLocator routes(RouteLocatorBuilder rlb) {
//     return rlb.routes()
//       // encaminha para o USER-SERVICE (8081)
//       .route("user-auth", r -> r.path("/api/auth/**")
//         .uri("http://localhost:8081"))
//       .route("user-users", r -> r.path("/api/users/**")
//         .uri("http://localhost:8081"))
//       // encaminha para o CARD-SERVICE (8082)
//       .route("card-api",  r -> r.path("/api/cards/**")
//         .uri("http://localhost:8082"))
//       .build();
//   }
// }

// backend/gateway/src/main/java/com/example/gateway/GatewayApplication.java
// package com.example.gateway;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cloud.gateway.route.RouteLocator;
// import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
// import org.springframework.context.annotation.Bean;

// @SpringBootApplication
// public class GatewayApplication {

//   public static void main(String[] args) {
//     SpringApplication.run(GatewayApplication.class, args);
//   }

//   @Bean
//   RouteLocator routes(RouteLocatorBuilder rlb) {
//     return rlb.routes()
//       .route("user-auth", r -> r.path("/api/auth/**")
//         .filters(f -> f.stripPrefix(1))
//         .uri("http://localhost:8081"))

//       .route("user-users", r -> r.path("/api/users/**")
//         .filters(f -> f.stripPrefix(1))
//         .uri("http://localhost:8081"))

//       .route("card-api",  r -> r.path("/api/cards/**")
//         .filters(f -> f.stripPrefix(1))
//         .uri("http://localhost:8082"))
//       .build();
//   }
// }

// backend/gateway/src/main/java/com/example/gateway/GatewayApplication.java
package com.example.gateway;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

@SpringBootApplication
public class GatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, args);
  }

  // ✅ CORS global no Gateway
  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of("http://localhost:4200"));
    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource src =
        new UrlBasedCorsConfigurationSource(new PathPatternParser());
    src.registerCorsConfiguration("/**", cfg);
    return new CorsWebFilter(src);
  }

  // ✅ Rotas: stripPrefix(1) remove o "/api" antes de mandar para os serviços
  @Bean
  RouteLocator routes(RouteLocatorBuilder rlb) {
    return rlb.routes()
      .route("user-auth", r -> r.path("/api/auth/**")
        .filters(f -> f.stripPrefix(1))
        .uri("http://localhost:8081"))
      .route("user-users", r -> r.path("/api/users/**")
        .filters(f -> f.stripPrefix(1))
        .uri("http://localhost:8081"))
      .route("card-api",  r -> r.path("/api/cards/**")
        .filters(f -> f.stripPrefix(1))
        .uri("http://localhost:8082"))
      .build();
  }
}


