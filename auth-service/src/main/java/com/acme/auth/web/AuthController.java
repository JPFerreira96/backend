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
  public record LoginRequest(String email, String password) {
  }

  private final UserClient users;
  private final JwtService jwt;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public AuthController(UserClient users, JwtService jwt) {
    this.users = users;
    this.jwt = jwt;
  }

  // @PostMapping("/login")
  // public Map<String, String> login(@RequestBody LoginRequest req) {

  // System.out.println("[AUTH] Tentando login para " + req.email());

  // var u = users.findByEmailOrNull(req.email()); // pode vir null
  // if (u == null) {
  // System.out.println("[AUTH] Usuário não encontrado");
  // throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais
  // inválidas");
  // }

  // boolean ok = encoder.matches(req.password(), u.getPasswordHash());
  // System.out.println("[AUTH] BCrypt confere? " + ok);

  // if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
  // "Credenciais inválidas");

  // var token = jwt.issue(u.getId(), u.getRole());
  // return Map.of("token", token);
  // }

  @PostMapping("/login")
  public Map<String, String> login(@RequestBody LoginRequest req) {
    System.out.println("Login de " + req.email());
    var u = users.findByEmail(req.email());
    if (u == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    if (!encoder.matches(req.password(), u.getPasswordHash()))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    var token = jwt.issue(u.getId(), u.getRole());
    return Map.of("token", token);
  }
}
