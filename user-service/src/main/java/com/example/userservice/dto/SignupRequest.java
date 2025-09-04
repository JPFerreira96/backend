package com.example.userservice.dto;

import com.example.userservice.model.Role;

public class SignupRequest {
  public String name; public String email; public String password;
  public Role role; // opcional
}
