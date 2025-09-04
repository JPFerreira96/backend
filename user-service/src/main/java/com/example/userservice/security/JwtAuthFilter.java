// src/main/java/com/example/userservice/security/JwtAuthFilter.java
package com.example.userservice.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  @Autowired JwtUtil jwt;
  @Autowired UserRepository repo;

  @Override
  // protected boolean shouldNotFilter(HttpServletRequest req) {
  //   // Com context-path /api, o servletPath será /auth/login ou /auth/signup
  //   String p = req.getServletPath();
  //   return p.startsWith("/auth/");
  // }

//   protected boolean shouldNotFilter(HttpServletRequest req) {
//   // caminho inteiro, incluindo (ou não) o context-path
//   String uri = req.getRequestURI();
//   // Ignora /api/auth/* e /auth/*
//   return uri.startsWith("/api/auth/") || uri.startsWith("/auth/");
// }

protected boolean shouldNotFilter(HttpServletRequest req) {
  String uri = req.getRequestURI();
  return uri.startsWith("/api/auth/") || uri.startsWith("/auth/");
}

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    String auth = req.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);
      String email = jwt.extractUsername(token);

      if (email != null && jwt.validateToken(token, email)
          && SecurityContextHolder.getContext().getAuthentication() == null) {

        User u = repo.findByEmail(email);
        if (u != null) {
          var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
          var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    }
    chain.doFilter(req, res);
  }
}
