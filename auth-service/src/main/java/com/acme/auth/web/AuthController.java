// package com.acme.auth.web;

// import java.util.Map;

// import org.springframework.http.HttpStatus;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.server.ResponseStatusException;

// import com.acme.auth.security.JwtService;
// import com.acme.auth.service.UserClient;

// @RestController
// @RequestMapping("/api/auth")
// public class AuthController {
//   public record LoginRequest(String email, String password) {
//   }

//   private final UserClient users;
//   private final JwtService jwt;
//   private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

//   public AuthController(UserClient users, JwtService jwt) {
//     this.users = users;
//     this.jwt = jwt;
//   }

//   // @PostMapping("/login")
//   // public Map<String, String> login(@RequestBody LoginRequest req) {

//   // System.out.println("[AUTH] Tentando login para " + req.email());

//   // var u = users.findByEmailOrNull(req.email()); // pode vir null
//   // if (u == null) {
//   // System.out.println("[AUTH] Usuário não encontrado");
//   // throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais
//   // inválidas");
//   // }

//   // boolean ok = encoder.matches(req.password(), u.getPasswordHash());
//   // System.out.println("[AUTH] BCrypt confere? " + ok);

//   // if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
//   // "Credenciais inválidas");

//   // var token = jwt.issue(u.getId(), u.getRole());
//   // return Map.of("token", token);
//   // }

//   @PostMapping("/signup")
//   public Map<String, Object> signup(@RequestBody SignupRequest req) {
//     System.out.println("Signup de " + req.email());
    
//     // Verificar se usuário já existe
//     var existingUser = users.findByEmail(req.email());
//     if (existingUser != null) {
//       throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
//     }
    
//     try {
//       // Hash da senha
//       // String hashedPassword = encoder.encode(req.password());
      
//       // Criar usuário no user-service
//       var newUser = users.createUser(req.name(), req.email(), req.password(), req.role());
      
//       // Gerar token JWT para o usuário criado
//       var token = jwt.issue(newUser.getId(), newUser.getRole());
      
//       var userInfo = Map.of(
//           "id", newUser.getId(),
//           "name", req.name(),
//           "email", newUser.getEmail(),
//           "role", newUser.getRole()
//       );
      
//       return Map.<String, Object>of(
//         "message", "Usuário criado com sucesso!",
//         "status", "success",
//         "email", req.email(),
//         "token", token,
//         "user", userInfo
//       );
      
//     } catch (IllegalArgumentException e) {
//       throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
//     } catch (Exception e) {
//       System.err.println("Erro ao criar usuário: " + e.getMessage());
//       e.printStackTrace();
//       throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao criar usuário");
//     }
//   }

//   // @PostMapping("/login")
//   // public Map<String, String> login(@RequestBody LoginRequest req) {
//   //   System.out.println("Login de " + req.email());
//   //   var u = users.findByEmail(req.email());
//   //   System.out.println("Usuário encontrado: " + (u != null ? u.getEmail() : "null"));
//   //   if (u == null)
//   //     throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
//   //   if (!encoder.matches(req.password(), u.getPasswordHash()))
//   //     throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
//   //   var token = jwt.issue(u.getId(), u.getRole());
//   //   return Map.of("token", token);
//   // }

//   @PostMapping("/login")
//     public Map<String, String> login(@RequestBody LoginRequest req) {
//       System.out.println("Login de " + req.email());
//       var u = users.findByEmail(req.email());
//       if (u == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");

//       if (!encoder.matches(req.password(), u.getPasswordHash()))
//         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");

//       String role = u.getRole();
//       if (role != null && !role.startsWith("ROLE_")) {
//         role = "ROLE_" + role.toUpperCase();
//       }

//       var token = jwt.issue(u.getId(), role);
//       return Map.of("token", token);
// }

//   public record SignupRequest(String name, String email, String password, String role) {}
// }
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

    // envia senha CRUA; user-service vai hashear
    var newUser = users.createUser(req.name(), req.email(), req.password(), req.role());

    // normaliza role para emitir no JWT
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

  // @PostMapping("/login")
  // public Map<String, Object> login(@RequestBody LoginRequest req) {
  //   System.out.println("[AUTH] Login de " + req.email());
  //   var u = users.findByEmail(req.email());
  //   if (u == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");

  //   System.out.println("[AUTH] Hash no banco: " + u.getPasswordHash());
  //   boolean ok = encoder.matches(req.password(), u.getPasswordHash());
  //   System.out.println("[AUTH] BCrypt confere? " + ok);
  //   if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");

  //   String role = u.getRole();
  //   if (role != null && !role.startsWith("ROLE_")) role = "ROLE_" + role.toUpperCase();

  //   var token = jwt.issue(u.getId(), role);

  //   var userInfo = Map.of(
  //     "id", u.getId(),
  //     "name", u.getEmail(), // se quiser buscar name depois, ajuste aqui
  //     "email", u.getEmail(),
  //     "role", role.equals("ROLE_ADMIN") ? "ADMIN" : "USER"
  //   );

  //   return Map.of(
  //     "token", token,
  //     "tokenType", "Bearer",
  //     "user", userInfo
  //   );
  // }

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
