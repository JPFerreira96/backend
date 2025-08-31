package com.acme.user.web;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.user.security.JwtService;
import com.acme.user.service.UserService;
import com.acme.user.web.dto.UserDTOs.CreateUserRequest;
import com.acme.user.web.dto.UserDTOs.UpdateUserRequest;
import com.acme.user.web.dto.UserDTOs.UserResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController @RequestMapping("/api/users") @Tag(name="Users")
public class UserController {
  private final UserService svc; private final JwtService jwt;
  public UserController(UserService svc, JwtService jwt){ this.svc=svc; this.jwt=jwt; }

  @GetMapping @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public List<UserResponse> list(){ return svc.list(); }

  @PostMapping @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req){
    return ResponseEntity.ok(svc.create(req, true));
  }

  @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest req, Principal p){
    var authId = UUID.fromString(p.getName());
    var isAdmin = false;
    return svc.update(id, req, authId, isAdmin);
  }

  @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable UUID id){ svc.delete(id); }
}
