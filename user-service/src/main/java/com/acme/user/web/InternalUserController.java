// package com.acme.user.web;

// import java.util.Map;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestHeader;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.acme.user.service.UserService;

// @RestController
// @RequestMapping("/api/internal/users")
// public class InternalUserController {

//   private final UserService svc;
//   private final String internalSecret;
//   private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

//   public InternalUserController(UserService svc,
//       @Value("${internal.secret}") String internalSecret) {
//     this.svc = svc;
//     this.internalSecret = internalSecret;
//   }

//   private void assertSecret(String secret) {
//     if (secret == null || !secret.equals(internalSecret)) {
//       throw new RuntimeException("forbidden");
//     }
//   }

//   @GetMapping("/by-email")
//   public Map<String, String> byEmail(
//       @RequestHeader("X-Internal-Secret") String secret,
//       @RequestParam String email) {
//     assertSecret(secret);

//     var u = svc.internalFindByEmail(email);
//     if (u == null) return null;

//     return Map.of(
//       "id", u.getId().toString(),
//       "email", u.getEmail(),
//       "passwordHash", u.getPasswordHash(),
//       "role", u.getRole()
//     );
//   }

//   // ======= AJUSTE AQUI: recebe password (crua), hasheia e cria =======
//   public static class CreateReq {
//     public String name;
//     public String email;
//     public String password; // senha crua
//     public String role;     // pode vir "user" | "ROLE_USER"
//   }

//   @PostMapping("/create")
//   public Map<String, String> create(
//       @RequestHeader("X-Internal-Secret") String secret,
//       @RequestBody CreateReq req) {
//     assertSecret(secret);

//     // normaliza role para ROLE_*
//     String role = normalizeRole(req.role);

//     // hashea AQUI (ou no service)
//     String hash = encoder.encode(req.password);

//     // monta DTO de criação
//     var createUserReq = new com.acme.user.web.dto.UserDTOs.CreateUserRequest();
//     createUserReq.name = req.name;
//     createUserReq.email = req.email;
//     createUserReq.password = hash; // <-- já vai com BCrypt
//     createUserReq.role = role;

//     var user = svc.internalCreateUser(createUserReq); // NÃO precisa changePassword depois

//     return Map.of(
//       "id", user.getId().toString(),
//       "email", user.getEmail(),
//       "passwordHash", user.getPasswordHash(),
//       "role", user.getRole()
//     );
//   }

//   private String normalizeRole(String role) {
//     if (role == null || role.isBlank()) return "ROLE_USER";
//     return role.startsWith("ROLE_") ? role : ("ROLE_" + role.toUpperCase());
//   }
// }

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
    public String password; // CRUA
    public String role;     // "user" ou "ROLE_USER"
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
    dto.password = req.password;  // CRUA -> service vai hashear
    dto.role = req.role;          // normaliza no service

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

