package com.acme.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import jakarta.annotation.PostConstruct;

@Service
public class UserClient {

  private final RestClient http;
  private final String internalSecret;
  private final String baseUrl;

  public static class InternalUser {
    private String id;
    private String email;
    private String passwordHash;
    private String role;
    public String getId(){ return id; }
    public String getEmail(){ return email; }
    public String getPasswordHash(){ return passwordHash; }
    public String getRole(){ return role; }
  }

  public UserClient(
      @Value("${services.user.base-url:http://localhost:8082}") String baseUrl,
      @Value("${internal.secret:}") String internalSecret) {
    this.http = RestClient.builder().baseUrl(baseUrl).build();
    this.internalSecret = internalSecret;
    this.baseUrl = baseUrl;
  }

  @PostConstruct
  void checkConfig() {
    if (internalSecret == null || internalSecret.isBlank()) {
      throw new IllegalStateException("""
        internal.secret NÃO configurado no auth-service.
        Defina em application.yml (internal.secret) ou via env INTERNAL_API_SECRET.
        """);
    }
    System.out.println("[UserClient] baseUrl=" + baseUrl + "  secret? " + (internalSecret.isBlank() ? "NÃO" : "OK"));
  }

  /** Retorna null se não encontrar; lança erro claro em 403. */
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
}
