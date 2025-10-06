package com.acme.user.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(
  name="users" 
  )

public class User {
  
  @Id private UUID id = UUID.randomUUID();
  @Column(nullable=false, length=120) private String name;
  @Column(nullable=false, unique=true, length=160) private String email;
  @Column(name="password_hash", nullable=false, length=200) private String passwordHash;
  @Column(nullable=false, length=30) private String role = "ROLE_USER";

  protected User(){}
  public static User create(String name, String email, String passwordHash, String role){
    var u = new User(); 
    u.name=name; 
    u.email=email; 
    u.passwordHash=passwordHash; 
    u.role = role==null?"ROLE_USER":role; return u;
  }
  public UUID getId(){
    return id;
  } public String getName(){
    return name;
  } 
  
  public String getEmail(){
    return email;
  }
  public String getPasswordHash(){
    return passwordHash;
  } 
  
  public String getRole(){
    return role;
  }
  public void rename(String n){
    this.name=n;
  } 
  
  public void changeRole(String r){
    this.role=r;
  }
  
  public void changeEmail(String e) {
    this.email = e;
  }
  
  public void changePassword(String hashedPassword) {
    this.passwordHash = hashedPassword;
  }
}
