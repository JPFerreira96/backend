package com.acme.user.web;

import com.acme.user.domain.User;
import com.acme.user.web.dto.UserDTOs.UserResponse;

public class UserMapper {
  public static UserResponse toResponse(User u){
    var dto = new UserResponse();
    dto.id=u.getId(); dto.name=u.getName(); dto.email=u.getEmail(); dto.role=u.getRole();
    return dto;
  }
}
