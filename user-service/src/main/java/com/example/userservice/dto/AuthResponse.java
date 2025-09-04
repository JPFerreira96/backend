package com.example.userservice.dto;

import com.example.userservice.model.Role;

public class AuthResponse {
  public String token;
  public String tokenType = "Bearer";
  public long expiresInMs;
  public UserView user;

  public static class UserView {
    public Long id;
    public String name;
    public String email;
    public Role role;

    public UserView(
        Long id,
        String name,
        String email,
        Role role) {
      this.id = id;
      this.name = name;
      this.email = email;
      this.role = role;
    }
  }

  public AuthResponse(String token, long expiresInMs, UserView user) {
    this.token = token;
    this.expiresInMs = expiresInMs;
    this.user = user;
  }
}
