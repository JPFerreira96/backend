package com.acme.user.web.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public class UserDTOs {
  public static class UserResponse {
    public UUID id; public String name; public String email; public String role;
  }
  public static class CreateUserRequest {
    @NotBlank @Size(max=120) public String name;
    @NotBlank @Email @Size(max=160) public String email;
    @NotBlank @Size(min=8, max=72) public String password;
    public String role; // só aceito via endpoint ADMIN
  }
  public static class UpdateUserRequest {
    @NotBlank @Size(max=120) public String name;
  }
}
