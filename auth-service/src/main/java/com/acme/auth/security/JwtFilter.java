package com.acme.auth.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter implements Filter {
  private final JwtService jwt;
  public JwtFilter(JwtService jwt){ this.jwt = jwt; }

  @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    var req = (HttpServletRequest) request;
    var h = req.getHeader("Authorization");
    if (h != null && h.startsWith("Bearer ")) {
      try {
        var claims = jwt.parse(h.substring(7)).getPayload();
        var role = String.valueOf(claims.get("role"));
        var auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (Exception ignored) {}
    }
    chain.doFilter(request, response);
  }
}
