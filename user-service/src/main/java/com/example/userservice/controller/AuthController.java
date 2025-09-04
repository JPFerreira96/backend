package com.example.userservice.controller;

import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.SignupRequest;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtUtil;

@RestController
@RequestMapping("/auth") // endpoint final = /api/auth/... (você tem context-path /api)
public class AuthController {
  private final UserRepository repo;
  private final PasswordEncoder encoder;
  private final JwtUtil jwt;

  public AuthController(UserRepository repo, PasswordEncoder encoder, JwtUtil jwt) {
    this.repo = repo; this.encoder = encoder; this.jwt = jwt;
  }

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest r) {
    if (repo.findByEmail(r.email) != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
    }
    User u = new User();
    u.setName(r.name);
    u.setEmail(r.email);
    u.setPassword(encoder.encode(r.password));
    if (r.role != null) u.setRole(r.role);
    repo.save(u);

    String token = jwt.generateToken(u.getEmail());
    var view = new AuthResponse.UserView(u.getId(), u.getName(), u.getEmail(), u.getRole());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new AuthResponse(token, jwt.getExpirationMs(), view));
  }

  @PostMapping("/login")
  public AuthResponse login(@RequestBody LoginRequest r) {
    User u = repo.findByEmail(r.email);
    if (u == null || !encoder.matches(r.password, u.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }
    String token = jwt.generateToken(u.getEmail());
    var view = new AuthResponse.UserView(u.getId(), u.getName(), u.getEmail(), u.getRole());
    return new AuthResponse(token, jwt.getExpirationMs(), view);
  }
}
