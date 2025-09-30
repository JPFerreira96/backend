package com.acme.user.web.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.acme.user.domain.User;
import com.acme.user.repository.UserRepository;
import com.acme.user.service.CardClient;
import com.acme.user.web.dto.UserDTOs.AddCardToUserRequest;
import com.acme.user.web.dto.UserDTOs.CardSummary;
import com.acme.user.web.dto.UserDTOs.CreateUserRequest;
import com.acme.user.web.dto.UserDTOs.TipoCartao;
import com.acme.user.web.dto.UserDTOs.UpdateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Testes de integração simplificados para UserController
 * Testa a integração completa entre Controller, Service e Repository
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private CardClient cardClient;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = User.create("João Silva", "joao@email.com", "hashedPassword", "ROLE_USER");
        testUser = userRepository.save(testUser);
        
        // Mock CardClient para evitar chamadas externas
        when(cardClient.getUserCards(any(UUID.class))).thenReturn(List.of());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("João Silva")))
                .andExpect(jsonPath("$.email", is("joao@email.com")));
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_ShouldCreateUser_WhenValidRequest() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.name = "Maria Santos";
        request.email = "maria@email.com";
        request.password = "senha123";

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Maria Santos")))
                .andExpect(jsonPath("$.email", is("maria@email.com")));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenEmailExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.name = "João Duplicado";
        request.email = "joao@email.com"; // Email já existe
        request.password = "senha123";

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenValidRequest() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.name = "João Silva Atualizado";
        request.email = "joao.novo@email.com";

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("João Silva Atualizado")))
                .andExpect(jsonPath("$.email", is("joao.novo@email.com")));
    }

    @Test
    void addCardToUser_ShouldAddCard_WhenValidRequest() throws Exception {
        AddCardToUserRequest request = new AddCardToUserRequest();
        request.numeroCartao = "1234.5678.9012.3456";
        request.nome = "Cartão Trabalho";
        request.tipoCartao = TipoCartao.TRABALHADOR;

        CardSummary cardSummary = new CardSummary();
        cardSummary.numeroCartao = request.numeroCartao;
        cardSummary.nome = request.nome;

        when(cardClient.createCard(any(UUID.class), any(String.class), any(String.class), any(String.class)))
            .thenReturn(cardSummary);

        mockMvc.perform(post("/api/users/{id}/cards", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.numeroCartao", is("1234.5678.9012.3456")))
                .andExpect(jsonPath("$.nome", is("Cartão Trabalho")));
    }

    @Test
    void removeCardFromUser_ShouldRemoveCard() throws Exception {
        UUID cardId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/{userId}/cards/{cardId}", testUser.getId(), cardId))
                .andExpect(status().isNoContent());
    }

    @Test
    void toggleCardStatus_ShouldToggleStatus() throws Exception {
        UUID cardId = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/{userId}/cards/{cardId}/toggle-status", testUser.getId(), cardId)
                .param("activate", "true"))
                .andExpect(status().isNoContent());
    }
}