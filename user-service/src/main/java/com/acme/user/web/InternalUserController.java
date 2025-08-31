package com.acme.user.web;

import com.acme.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

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
}
