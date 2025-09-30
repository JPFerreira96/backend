package com.acme.user.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.user.service.UserService;

@RestController @RequestMapping("/api/internal/users")
public class InternalUserController {
  private final UserService svc; private final String internalSecret;
  public InternalUserController(UserService svc, @Value("${internal.secret}") String internalSecret){
    this.svc=svc; this.internalSecret=internalSecret;
  }

  @GetMapping("/by-email")
  public Map<String,String> byEmail(@RequestHeader("X-Internal-Secret") String secret, @RequestParam String email){
    if(!internalSecret.equals(secret)) throw new RuntimeException("forbidden");
    var u = svc.internalFindByEmail(email);
    if(u==null) return null;
    return Map.of("id", u.getId().toString(), "email", u.getEmail(), "passwordHash", u.getPasswordHash(), "role", u.getRole());
  }

  @PostMapping("/create")
  public Map<String,String> create(@RequestHeader("X-Internal-Secret") String secret, @RequestBody Map<String,String> req){
    if(!internalSecret.equals(secret)) throw new RuntimeException("forbidden");
    
    var createUserReq = new com.acme.user.web.dto.UserDTOs.CreateUserRequest();
    createUserReq.name = req.get("name");
    createUserReq.email = req.get("email");
    createUserReq.password = "temp"; // será substituído pelo hash já fornecido
    createUserReq.role = req.get("role");
    
    // Criar usuário através do serviço interno
    var user = svc.internalCreateUser(createUserReq);
    // Update with the provided password hash
    user.changePassword(req.get("passwordHash"));
    
    return Map.of("id", user.getId().toString(), 
                  "email", user.getEmail(), 
                  "passwordHash", user.getPasswordHash(), 
                  "role", user.getRole());
  }
}
