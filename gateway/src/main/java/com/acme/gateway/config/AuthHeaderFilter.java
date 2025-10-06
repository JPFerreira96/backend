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
        String path = request.getPath().value();
        String method = request.getMethod().toString();
        
        System.out.println("Gateway - " + method + " " + path);
        
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null) {
            System.out.println(" Token presente: " + authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
        } else {
            System.out.println("Sem token de autenticação");
        }        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}