package com.acme.user.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.user.service.UserService;
import com.acme.user.web.dto.UserDTOs.AddCardToUserRequest;
import com.acme.user.web.dto.UserDTOs.CardSummary;
import com.acme.user.web.dto.UserDTOs.ChangePasswordRequest;
import com.acme.user.web.dto.UserDTOs.CreateUserRequest;
import com.acme.user.web.dto.UserDTOs.UpdateUserRequest;
import com.acme.user.web.dto.UserDTOs.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller REST para gerenciamento de usuários
 * Implementa todas as operações CRUD e gerenciamento de cartões
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "API para gerenciamento de usuários")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // === OPERAÇÕES CRUD DE USUÁRIOS ===

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Lista todos os usuários", description = "Retorna uma lista de todos os usuários cadastrados")
    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Busca usuário por ID", description = "Retorna os dados de um usuário específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public UserResponse getUserById(
            @Parameter(description = "ID do usuário") @PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um novo usuário", description = "Cria um novo usuário no sistema (apenas ADMIN)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Email já está em uso")
    })
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "Dados do usuário a ser criado") 
            @Valid @RequestBody CreateUserRequest request) {
        
        UserResponse user = userService.createUser(request, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Atualiza um usuário", description = "Atualiza os dados de um usuário existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public UserResponse updateUser(
            @Parameter(description = "ID do usuário") @PathVariable UUID id,
            @Parameter(description = "Dados a serem atualizados") @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        return userService.updateUser(id, request, authUserId, isAdmin);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um usuário", description = "Remove um usuário do sistema (apenas ADMIN)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID do usuário") @PathVariable UUID id) {
        
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Altera senha do usuário", description = "Altera a senha de um usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Senha atual incorreta"),
        @ApiResponse(responseCode = "403", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "ID do usuário") @PathVariable UUID id,
            @Parameter(description = "Dados para alteração de senha") @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        userService.changePassword(id, request, authUserId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    // === OPERAÇÕES COM CARTÕES ===

    @PostMapping("/{userId}/cards")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Adiciona cartão ao usuário", description = "Adiciona um novo cartão de ônibus ao usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cartão adicionado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados do cartão inválidos"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<CardSummary> addCardToUser(
            @Parameter(description = "ID do usuário") @PathVariable UUID userId,
            @Parameter(description = "Dados do cartão") @Valid @RequestBody AddCardToUserRequest request,
            Authentication authentication) {
        
        // Verificar se é o próprio usuário ou admin
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        if (!isAdmin && !authUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        CardSummary card = userService.addCardToUser(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @DeleteMapping("/{userId}/cards/{cardId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Remove cartão do usuário", description = "Remove um cartão de ônibus do usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cartão removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário ou cartão não encontrado")
    })
    public ResponseEntity<Void> removeCardFromUser(
            @Parameter(description = "ID do usuário") @PathVariable UUID userId,
            @Parameter(description = "ID do cartão") @PathVariable UUID cardId,
            Authentication authentication) {
        
        // Verificar se é o próprio usuário ou admin
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        if (!isAdmin && !authUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        userService.removeCardFromUser(userId, cardId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/cards/{cardId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Ativa cartão do usuário", description = "Ativa um cartão de ônibus do usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cartão ativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário ou cartão não encontrado")
    })
    public ResponseEntity<Void> activateCard(
            @Parameter(description = "ID do usuário") @PathVariable UUID userId,
            @Parameter(description = "ID do cartão") @PathVariable UUID cardId,
            Authentication authentication) {
        
        // Verificar se é o próprio usuário ou admin
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        if (!isAdmin && !authUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        userService.toggleCardStatus(userId, cardId, true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/cards/{cardId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Desativa cartão do usuário", description = "Desativa um cartão de ônibus do usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cartão desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário ou cartão não encontrado")
    })
    public ResponseEntity<Void> deactivateCard(
            @Parameter(description = "ID do usuário") @PathVariable UUID userId,
            @Parameter(description = "ID do cartão") @PathVariable UUID cardId,
            Authentication authentication) {
        
        // Verificar se é o próprio usuário ou admin
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        if (!isAdmin && !authUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        userService.toggleCardStatus(userId, cardId, false);
        return ResponseEntity.noContent().build();
    }
}
