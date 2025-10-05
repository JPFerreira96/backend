package com.acme.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201"}, allowCredentials = "true")
public class UserProxyController {

    private final WebClient webClient;

    public UserProxyController(WebClient.Builder webClientBuilder,
                               @Value("${USER_SERVICE_URL:http://localhost:8084}") String userServiceUrl) {
        this.webClient = webClientBuilder
            .baseUrl(userServiceUrl)
            .build();
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<String>> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        System.out.println("🎯 UserProxyController - GET /me chamado");
        System.out.println("🔑 Authorization: " + (authHeader != null ? "PRESENTE" : "AUSENTE"));
        
        WebClient.RequestHeadersSpec<?> request = webClient.get()
            .uri("/api/users/me");
            
        if (authHeader != null) {
            request = request.header("Authorization", authHeader);
        }
        
        return request
            .retrieve()
            .toEntity(String.class)
            .doOnSuccess(response -> System.out.println("✅ User Service respondeu: " + response.getStatusCode()))
            .doOnError(error -> System.out.println("❌ Erro do User Service: " + error.getMessage()))
            .onErrorResume(WebClientResponseException.class, ex -> {
                System.out.println("🔄 Retornando erro: " + ex.getStatusCode());
                return Mono.just(ResponseEntity.status(ex.getStatusCode())
                    .headers(headers -> headers.addAll(ex.getHeaders()))
                    .body(ex.getResponseBodyAsString()));
            });
    }

    @PutMapping("/me")
    public Mono<ResponseEntity<String>> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody String body) {
        
        System.out.println("🎯 UserProxyController - PUT /me chamado");
        System.out.println("🔑 Authorization: " + (authHeader != null ? "PRESENTE" : "AUSENTE"));
        
        WebClient.RequestBodySpec request = webClient.put()
            .uri("/api/users/me")
            .contentType(MediaType.APPLICATION_JSON);

        WebClient.RequestHeadersSpec<?> headersSpec = request.bodyValue(body);
            
        if (authHeader != null) {
            headersSpec = headersSpec.header("Authorization", authHeader);
        }
        
        return headersSpec
            .retrieve()
            .toEntity(String.class)
            .doOnSuccess(response -> System.out.println("✅ User Service respondeu: " + response.getStatusCode()))
            .doOnError(error -> System.out.println("❌ Erro do User Service: " + error.getMessage()))
            .onErrorResume(WebClientResponseException.class, ex -> {
                return Mono.just(ResponseEntity.status(ex.getStatusCode())
                    .headers(headers -> headers.addAll(ex.getHeaders()))
                    .body(ex.getResponseBodyAsString()));
            });
    }

    @PutMapping("/me/password")
    public Mono<ResponseEntity<String>> changeMyPassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody String body) {
        
        System.out.println("🎯 UserProxyController - PUT /me/password chamado");
        System.out.println("🔑 Authorization: " + (authHeader != null ? "PRESENTE" : "AUSENTE"));
        
        WebClient.RequestBodySpec request = webClient.put()
            .uri("/api/users/me/password")
            .contentType(MediaType.APPLICATION_JSON);

        WebClient.RequestHeadersSpec<?> headersSpec = request.bodyValue(body);
            
        if (authHeader != null) {
            headersSpec = headersSpec.header("Authorization", authHeader);
        }
        
        return headersSpec
            .retrieve()
            .toEntity(String.class)
            .doOnSuccess(response -> System.out.println("✅ User Service respondeu: " + response.getStatusCode()))
            .doOnError(error -> System.out.println("❌ Erro do User Service: " + error.getMessage()))
            .onErrorResume(WebClientResponseException.class, ex -> {
                return Mono.just(ResponseEntity.status(ex.getStatusCode())
                    .headers(headers -> headers.addAll(ex.getHeaders()))
                    .body(ex.getResponseBodyAsString()));
            });
    }
}