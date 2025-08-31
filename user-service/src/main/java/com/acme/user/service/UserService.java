package com.acme.user.service;

import com.acme.user.domain.User;
import com.acme.user.repository.UserRepository;
import com.acme.user.web.UserMapper;
import com.acme.user.web.dto.UserDTOs.*;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;

import java.util.List; import java.util.NoSuchElementException; import java.util.UUID;

@Service
public class UserService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private final UserRepository repo; private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  public UserService(UserRepository repo){ this.repo=repo; }

  public List<UserResponse> list(){ return repo.findAll().stream().map(UserMapper::toResponse).toList(); }

  @Transactional
  public UserResponse create(CreateUserRequest req, boolean isAdmin){
    if(!isAdmin && req.role!=null) throw new AccessDeniedException("role só pode ser definido por ADMIN");
    repo.findByEmail(req.email).ifPresent(u->{ throw new IllegalArgumentException("email em uso"); });
    var u = User.create(req.name, req.email, encoder.encode(req.password), isAdmin && req.role!=null ? req.role : "ROLE_USER");
    return UserMapper.toResponse(repo.save(u));
  }

  @Transactional
  public UserResponse update(UUID id, UpdateUserRequest req, UUID authUserId, boolean isAdmin){
    var u = repo.findById(id).orElseThrow(()->new NoSuchElementException("user não encontrado"));
    if(!isAdmin && !u.getId().equals(authUserId)) throw new AccessDeniedException("não autorizado");
    u.rename(req.name);
    return UserMapper.toResponse(u);
  }

  @Transactional public void delete(UUID id){ repo.deleteById(id); }

  public User internalFindByEmail(String email){ return repo.findByEmail(email).orElse(null); }
}
