package com.acme.card.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
  private final SecretKey key;            // <-- SecretKey
  private final String issuer;
  private final String audience;
  private final int accessTtlMinutes;

  public JwtService() {

    Dotenv dotenv = Dotenv.load();
    String secretBase64 = dotenv.get("JWT_SECRET_BASE64");
    
    if (secretBase64 == null || secretBase64.isEmpty()) {
        throw new IllegalStateException("JWT_SECRET_BASE64 is missing from .env file");
    }

    byte[] bytes = Decoders.BASE64.decode(secretBase64);
    
    if (bytes.length < 32) {
        throw new IllegalStateException("Secret must be >= 256 bits");
    }
    
    this.key = Keys.hmacShaKeyFor(bytes);
    this.issuer = dotenv.get("JWT_ISSUER", "your-real-issuer-name");
    this.audience = dotenv.get("JWT_AUDIENCE", "your-real-audience-name");
    this.accessTtlMinutes = Integer.parseInt(dotenv.get("JWT_ACCESS_TTL_MINUTES", "15"));
  }
  public String issue(String subject, String role){
    Instant now = Instant.now();
    return Jwts.builder()
      .subject(subject)
      .issuer(issuer)
      .audience().add(audience).and()
      .claim("role", role)
      .id(UUID.randomUUID().toString())
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(accessTtlMinutes, ChronoUnit.MINUTES)))
      .signWith(key, Jwts.SIG.HS256)   // <-- ok com SecretKey
      .compact();
  }

  public Jws<Claims> parse(String token){
    return Jwts.parser()
      .requireIssuer(issuer)
      .requireAudience(audience)
      .verifyWith(key)                // <-- recebe SecretKey
      .build()
      .parseSignedClaims(token);
  }
}
