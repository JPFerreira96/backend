package com.acme.user.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter implements Filter {
  private final JwtService jwt;

  public JwtFilter(JwtService jwt) {
    this.jwt = jwt;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    var req = (HttpServletRequest) request;
    var res = (HttpServletResponse) response;

    String auth = req.getHeader("Authorization");

    // 👉 Early return aqui
    if (auth == null || !auth.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    try {
      var claims = jwt.parse(auth.substring(7)).getPayload();
      var role = String.valueOf(claims.get("role"));
      var authority = role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();
      var authToken = new UsernamePasswordAuthenticationToken(
          claims.getSubject(), null, List.of(new SimpleGrantedAuthority(authority)));
      SecurityContextHolder.getContext().setAuthentication(authToken);
      chain.doFilter(request, response);
    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}
