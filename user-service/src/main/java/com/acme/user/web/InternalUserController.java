package com.acme.user.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acme.user.domain.User;
import com.acme.user.service.UserService;

@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {

  private final UserService svc;
  private final String internalSecret;

  public InternalUserController(UserService svc,
      @Value("${internal.secret}") String internalSecret) {
    this.svc = svc;
    this.internalSecret = internalSecret;
  }

  private void assertSecret(String secret) {
    if (secret == null || !secret.equals(internalSecret)) {
      throw new RuntimeException("forbidden");
    }
  }

  @GetMapping("/by-email")
  public Map<String, String> byEmail(
    @RequestHeader("X-Internal-Secret") String secret,
    @RequestParam String email) {
    assertSecret(secret);

    var u = svc.internalFindByEmail(email);
    if (u == null) return null;

    return toResponse(u);
  }

  public static class CreateReq {
    public String name;
    public String email;
    public String password;
    public String role;
  }

  public static class VerifyReq {
    public String email;
    public String password;
  }

  @PostMapping("/create")
  public Map<String, String> create(
      @RequestHeader("X-Internal-Secret") String secret,
      @RequestBody CreateReq req) {
    assertSecret(secret);

    var dto = new com.acme.user.web.dto.UserDTOs.CreateUserRequest();
    dto.name = req.name;
    dto.email = req.email;
    dto.password = req.password;
    dto.role = req.role;

    var user = svc.internalCreateUser(dto);

    return toResponse(user);
  }

  @PostMapping("/verify")
  public Map<String, String> verify(
      @RequestHeader("X-Internal-Secret") String secret,
      @RequestBody VerifyReq req) {
    assertSecret(secret);

    var user = svc.internalVerifyCredentials(req.email, req.password);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }

    return toResponse(user);
  }
  
  private Map<String, String> toResponse(User user) {
    return Map.of(
      "id", user.getId().toString(),
      "name", user.getName(),
      "email", user.getEmail(),
      "passwordHash", user.getPasswordHash(),
      "role", user.getRole()
    );
  }
}

