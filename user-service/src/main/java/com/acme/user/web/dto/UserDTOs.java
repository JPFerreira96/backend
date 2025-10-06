package com.acme.user.web.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserDTOs {
    
    public static class UserResponse {
        public UUID id;
        public String name;
        public String email;
        public String role;
        public List<CardSummary> cards;
        
        public UserResponse() {}
        
        public UserResponse(UUID id, String name, String email, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }
    
    public static class CreateUserRequest {
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        public String name;
        
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ter formato válido")
        @Size(max = 160, message = "Email deve ter no máximo 160 caracteres")
        public String email;
        
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
        public String password;
        
        public String role;
    }
    
    public static class UpdateUserRequest {
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        public String name;
        
        @Email(message = "Email deve ter formato válido")
        @Size(max = 160, message = "Email deve ter no máximo 160 caracteres")
        public String email;
    }
    
    public static class ChangePasswordRequest {
        @NotBlank(message = "Senha atual é obrigatória")
        public String currentPassword;
        
        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 8, max = 72, message = "Nova senha deve ter entre 8 e 72 caracteres")
        public String newPassword;
    }
    
    public static class CardSummary {
        public UUID id;
        public String numeroCartao;
        public String nome;
        public boolean status;
        public String tipoCartao;
        
        public CardSummary() {}
        
        public CardSummary(UUID id, String numeroCartao, String nome, boolean status, String tipoCartao) {
            this.id = id;
            this.numeroCartao = numeroCartao;
            this.nome = nome;
            this.status = status;
            this.tipoCartao = tipoCartao;
        }
    }
    
    public static class AddCardToUserRequest {
        @Size(max = 30, message = "Número do cartão deve ter no máximo 30 caracteres")
        public String numeroCartao;
        
        @Size(max = 120, message = "Nome do cartão deve ter no máximo 120 caracteres")
        public String nome;
        
        @NotNull(message = "Tipo do cartão é obrigatório")
        public TipoCartao tipoCartao;
    }
    
    public enum TipoCartao {
        COMUM, ESTUDANTE, TRABALHADOR
    }
}
