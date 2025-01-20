package com.nttbank.microservices.debitcardservice.controller;

import com.nttbank.microservices.debitcardservice.mapper.DebitCardMapper;
import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import com.nttbank.microservices.debitcardservice.model.entity.DebitCardTransactions;
import com.nttbank.microservices.debitcardservice.service.DebitCardService;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class DebitCardControllerTest {

  @Mock
  private DebitCardService service;

  @Mock
  private DebitCardMapper mapper;

  WebTestClient client;

  private static final String BASE_URL = "/debitcards";
  private static final String DEBIT_CARD_ID_PATH = "/{debit_card_id}";

  private DebitCard debitCard;


  @BeforeEach
  void setUp() {
    client = WebTestClient.bindToController(new DebitCardController(service, mapper))
        .build();
    debitCard = DebitCard.builder()
        .id("1234")
        .customerId("5678")
        .cardNumber("1234-5678-9012-3456")
        .cardholderName("John Doe")
        .expirationDate("01/23")
        .cvv("123")
        .mainAccountId("9876")
        .linkedAccounts(Set.of("1111", "2222"))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .status("active")
        .build();
  }

  @Test
  void findById_ShouldReturnDebitCard() {
    Mockito.when(service.findAll()).thenReturn(Flux.just(debitCard));

    client.get().uri(BASE_URL)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(DebitCard.class)
        .hasSize(1).contains(debitCard);

    Mockito.verify(service, Mockito.times(1)).findAll();
  }

}