package com.acme.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class AuthHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Log da requisição para debug
        System.out.println("🔗 Gateway - Request: " + request.getMethod() + " " + request.getPath());
        System.out.println("🔗 Gateway - Headers: " + request.getHeaders());
        
        // Verifica se tem Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null) {
            System.out.println("🔗 Gateway - Authorization header found: " + authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
        } else {
            System.out.println("🔗 Gateway - No Authorization header found");
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Execute before other filters
    }
}