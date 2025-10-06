package com.acme.auth.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acme.auth.security.JwtService;
import com.acme.auth.service.UserClient;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  public record LoginRequest(String email, String password) {}

  private final UserClient users;
  private final JwtService jwt;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public AuthController(UserClient users, JwtService jwt) {
    this.users = users;
    this.jwt = jwt;
  }

  @PostMapping("/signup")
  public Map<String, Object> signup(@RequestBody SignupRequest req) {
    var existing = users.findByEmail(req.email());
    if (existing != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
    }

    var newUser = users.createUser(req.name(), req.email(), req.password(), req.role());
    String role = newUser.getRole();
    if (role == null || role.isBlank()) role = "ROLE_USER";
    if (!role.startsWith("ROLE_")) role = "ROLE_" + role.toUpperCase();

    var token = jwt.issue(newUser.getId(), role);

    var userInfo = Map.of(
      "id", newUser.getId(),
      "name", newUser.getName() != null ? newUser.getName() : newUser.getEmail(),
      "email", newUser.getEmail(),
      "role", "ROLE_ADMIN".equals(role) ? "ADMIN" : "USER"
    );

    return Map.of(
      "message", "Usuário criado com sucesso!",
      "status", "success",
      "email", newUser.getEmail(),
      "token", token,
      "tokenType", "Bearer",
      "user", userInfo
    );
  }

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody LoginRequest req) {
    System.out.println("[AUTH] Login de " + req.email());
    var u = users.verifyCredentials(req.email(), req.password());

    if (u == null) {
      // Fallback para compatibilidade (caso o endpoint /verify ainda não esteja disponível)
      System.out.println("[AUTH] verifyCredentials retornou null, tentando fallback findByEmail()");
      u = users.findByEmail(req.email());
      var fallbackHash = u != null ? u.getPasswordHash() : null;
      if (u == null || fallbackHash == null || fallbackHash.isBlank() || !encoder.matches(req.password(), fallbackHash)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
      }
    }

    String role = u.getRole();
    if (role == null || role.isBlank()) role = "ROLE_USER";
    if (!role.startsWith("ROLE_")) role = "ROLE_" + role.toUpperCase();

    var token = jwt.issue(u.getId(), role);

    var userInfo = Map.of(
      "id", u.getId(),
      "name", u.getName() != null ? u.getName() : u.getEmail(),
      "email", u.getEmail(),
      "role", "ROLE_ADMIN".equals(role) ? "ADMIN" : "USER"
    );

    return Map.of(
      "token", token,
      "tokenType", "Bearer",
      "user", userInfo
    );
  }

  public record SignupRequest(String name, String email, String password, String role) {}
}
