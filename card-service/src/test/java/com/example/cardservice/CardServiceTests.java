package com.example.cardservice;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.cardservice.controller.CardController;
import com.example.cardservice.dto.CardDto;
import com.example.cardservice.model.Card;
import com.example.cardservice.service.CardService;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CardServiceTests {

    @Autowired
    private MockMvc mockMvc;

    private CardService cardService;

    private Card card;

    @BeforeEach
    public void setUp() {
        card = new Card(1L, 12345678901L, "Fulano", true, Card.TipoCartao.COMUM);
    }

    @Test
    public void testGetAllCards() throws Exception {
        List<Card> cards = Arrays.asList(card);
        when(cardService.getAllCards()).thenReturn(cards);

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nome").value("Fulano"));
    }

    @Test
    public void testCreateCard() throws Exception {
        // ajuste o nome do método conforme sua service (createCard/create/save)
        when(cardService.createCard(any(CardDto.class))).thenReturn(card);

        String payload = """
        { "userId": 1, "numeroCartao": 12345678901, "nome": "Fulano", "status": true, "tipoCartao": "COMUM" }
        """;

        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void testDeleteCard() throws Exception {
        doNothing().when(cardService).deleteCard(1L);

        mockMvc.perform(delete("/api/cards/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateCard() throws Exception {
        Card updatedCard = new Card(1L, 12345678901L, "Updated Card Name", true, Card.TipoCartao.COMUM);
        when(cardService.updateCard(anyLong(), any(CardDto.class))).thenReturn(updatedCard);

        mockMvc.perform(put("/api/cards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"numeroCartao\":12345678901,\"nome\":\"Updated Card Name\",\"status\":true,\"tipoCartao\":\"COMUM\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Updated Card Name"));
    }
}