// package com.acme.auth.service;

// import java.util.Map;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestClient;
// import org.springframework.web.client.RestClientResponseException;

// import jakarta.annotation.PostConstruct;

// @Service
// public class UserClient {

//   private final RestClient http;
//   private final String internalSecret;
//   private final String baseUrl;

//   public static class InternalUser {
//     private String id;
//     private String email;
//     private String password;
//     private String role;
//     public String getId(){ return id; }
//     public String getEmail(){ return email; }
//     public String getPasswordHash(){ return password; }
//     public String getRole(){ return role; }
//   }

//   public UserClient(
//       @Value("${services.user.base-url:http://localhost:8084}") String baseUrl,
//       @Value("${internal.secret:}") String internalSecret) {
//     this.http = RestClient.builder().baseUrl(baseUrl).build();
//     this.internalSecret = internalSecret;
//     this.baseUrl = baseUrl;
//   }

//   @PostConstruct
//   void checkConfig() {
//     if (internalSecret == null || internalSecret.isBlank()) {
//       throw new IllegalStateException(
//         "internal.secret NÃO configurado no auth-service. " +
//         "Defina em application.yml (internal.secret) ou via env INTERNAL_API_SECRET."
//         );
//     }
//     System.out.println("[UserClient] baseUrl=" + baseUrl + "  secret? " + (internalSecret.isBlank() ? "NÃO" : "OK"));
//   }

//   /** Retorna null se não encontrar; lança erro claro em 403. */
//   public InternalUser findByEmail(String email) {
//     try {
//       return http.get()
//           .uri("/api/internal/users/by-email?email={email}", email)
//           .header("X-Internal-Secret", internalSecret)
//           .accept(MediaType.APPLICATION_JSON)
//           .retrieve()
//           .body(InternalUser.class);
//     } catch (RestClientResponseException e) {
//       var sc = e.getStatusCode();
//       System.out.println("[UserClient] " + sc.value() + " ao buscar " + email +
//           " body=" + e.getResponseBodyAsString());
//       if (sc.equals(HttpStatus.NOT_FOUND)) return null;
//       if (sc.equals(HttpStatus.FORBIDDEN)) {
//         throw new IllegalStateException("Acesso interno negado (X-Internal-Secret divergente).");
//       }
//       throw e;
//     }
//   }

//   /** Cria um usuário através do endpoint interno */
//   public InternalUser createUser(String name, String email, String password, String role) {
//     try {
//       var createRequest = Map.of(
//         "name", name,
//         "email", email,
//         "password", password, 
//         "role", role != null ? role : "ROLE_USER"
//       );

//       return http.post()
//           .uri("/api/internal/users/create")
//           .header("X-Internal-Secret", internalSecret)
//           .contentType(MediaType.APPLICATION_JSON)
//           .body(createRequest)
//           .retrieve()
//           .body(InternalUser.class);
//     } catch (RestClientResponseException e) {
//       var sc = e.getStatusCode();
//       System.out.println("[UserClient] " + sc.value() + " ao criar usuário " + email +
//           " body=" + e.getResponseBodyAsString());
//       if (sc.equals(HttpStatus.CONFLICT)) {
//         throw new IllegalArgumentException("Email já em uso");
//       }
//       if (sc.equals(HttpStatus.FORBIDDEN)) {
//         throw new IllegalStateException("Acesso interno negado (X-Internal-Secret divergente).");
//       }
//       throw e;
//     }
//   }
// }

// auth-service/src/main/java/com/acme/auth/service/UserClient.java
package com.acme.auth.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.PostConstruct;

@Service
public class UserClient {

  private final RestClient http;
  private final String internalSecret;
  private final String baseUrl;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InternalUser {
    private String id;
    @JsonProperty("name")
    private String name;
    private String email;
    @JsonProperty("passwordHash")
    private String passwordHash;
    private String role;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
  }

  public UserClient(
      @Value("${services.user.base-url:http://localhost:8084}") String baseUrl,
      @Value("${internal.secret:}") String internalSecret) {
    this.http = RestClient.builder().baseUrl(baseUrl).build();
    this.internalSecret = internalSecret;
    this.baseUrl = baseUrl;
  }

  @PostConstruct
  void checkConfig() {
    if (internalSecret == null || internalSecret.isBlank()) {
      throw new IllegalStateException(
        "internal.secret NÃO configurado no auth-service. Defina em application.yml (internal.secret) ou via env INTERNAL_API_SECRET."
      );
    }
    System.out.println("[UserClient] baseUrl=" + baseUrl + "  secret? " + (internalSecret.isBlank() ? "NÃO" : "OK"));
  }

  public InternalUser findByEmail(String email) {
    try {
      return http.get()
          .uri("/api/internal/users/by-email?email={email}", email)
          .header("X-Internal-Secret", internalSecret)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(InternalUser.class);
    } catch (RestClientResponseException e) {
      var sc = e.getStatusCode();
      System.out.println("[UserClient] " + sc.value() + " ao buscar " + email +
          " body=" + e.getResponseBodyAsString());
      if (sc.equals(HttpStatus.NOT_FOUND)) return null;
      if (sc.equals(HttpStatus.FORBIDDEN)) {
        throw new IllegalStateException("Acesso interno negado (X-Internal-Secret divergente).");
      }
      throw e;
    }
  }

  public InternalUser verifyCredentials(String email, String password) {
    try {
      var payload = Map.of(
        "email", email,
        "password", password
      );

      return http.post()
          .uri("/api/internal/users/verify")
          .header("X-Internal-Secret", internalSecret)
          .contentType(MediaType.APPLICATION_JSON)
          .body(payload)
          .retrieve()
          .body(InternalUser.class);
    } catch (RestClientResponseException e) {
      var sc = e.getStatusCode();
      System.out.println("[UserClient] " + sc.value() + " ao verificar credenciais de " + email +
          " body=" + e.getResponseBodyAsString());
      if (sc.equals(HttpStatus.UNAUTHORIZED)) return null;
      if (sc.equals(HttpStatus.NOT_FOUND)) return null;
      if (sc.equals(HttpStatus.FORBIDDEN)) {
        throw new IllegalStateException("Acesso interno negado (X-Internal-Secret divergente).");
      }
      throw e;
    }
  }

  // Envia senha CRUA; user-service hasheia
  public InternalUser createUser(String name, String email, String password, String role) {
    try {
      var createRequest = Map.of(
        "name", name,
        "email", email,
        "password", password,   // CRUA
        "role", role != null ? role : "user"
      );

      return http.post()
          .uri("/api/internal/users/create")
          .header("X-Internal-Secret", internalSecret)
          .contentType(MediaType.APPLICATION_JSON)
          .body(createRequest)
          .retrieve()
          .body(InternalUser.class);
    } catch (RestClientResponseException e) {
      var sc = e.getStatusCode();
      System.out.println("[UserClient] " + sc.value() + " ao criar usuário " + email +
          " body=" + e.getResponseBodyAsString());
      if (sc.equals(HttpStatus.CONFLICT)) {
        throw new IllegalArgumentException("Email já em uso");
      }
      if (sc.equals(HttpStatus.FORBIDDEN)) {
        throw new IllegalStateException("Acesso interno negado (X-Internal-Secret divergente).");
      }
      throw e;
    }
  }
}

