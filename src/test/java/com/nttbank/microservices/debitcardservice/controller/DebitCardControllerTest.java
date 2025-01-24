package com.nttbank.microservices.debitcardservice.controller;

import static org.hamcrest.Matchers.hasItem;

import com.nttbank.microservices.debitcardservice.mapper.DebitCardMapper;
import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import com.nttbank.microservices.debitcardservice.model.record.DebitCardRecord;
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
  private DebitCardRecord debitCardRecord;


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
    debitCardRecord = DebitCardRecord.builder()
        .id("1234")
        .customerId("5678")
        .cardNumber("1234-5678-9012-3456")
        .cardholderName("John Doe")
        .expirationDate("01/23")
        .cvv("123")
        .mainAccountId("9876")
        .linkedAccounts(Set.of("1111", "2222"))
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

  @Test
  void save_ShouldCreateDebitCard() {

    Mockito.when(mapper.debitCardRecordToDebitCard(debitCardRecord)).thenReturn(debitCard);
    Mockito.when(service.save(Mockito.any(DebitCard.class))).thenReturn(Mono.just(debitCard));

    client.post().uri(BASE_URL)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(debitCardRecord)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().valueEquals("Location", BASE_URL + "/1234")
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.id").isEqualTo("1234")
        .jsonPath("$.customerId").isEqualTo("5678")
        .jsonPath("$.cardNumber").isEqualTo("1234-5678-9012-3456")
        .jsonPath("$.cardholderName").isEqualTo("John Doe");

    Mockito.verify(service, Mockito.times(1)).save(Mockito.any(DebitCard.class));
  }

  @Test
  void addLinkedAccount_ShouldAddAccountToDebitCard() {
    String debitCardId = "1234";
    String accountId = "3333";
    DebitCard updatedDebitCard = DebitCard.builder()
        .id(debitCardId)
        .customerId("5678")
        .cardNumber("1234-5678-9012-3456")
        .cardholderName("John Doe")
        .expirationDate("01/23")
        .cvv("123")
        .mainAccountId("9876")
        .linkedAccounts(Set.of("1111", "2222", accountId))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .status("active")
        .build();

    Mockito.when(service.addLinkedAccount(debitCardId, accountId))
        .thenReturn(Mono.just(updatedDebitCard));

    client.post().uri(BASE_URL + "/" + debitCardId + "/accounts/" + accountId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.id").isEqualTo(debitCardId)
        .jsonPath("$.linkedAccounts").isArray()
        .jsonPath("$.linkedAccounts").value(hasItem(accountId));

    Mockito.verify(service, Mockito.times(1)).addLinkedAccount(debitCardId, accountId);
  }

  @Test
  void removeLinkedAccount_ShouldRemoveAccountFromDebitCard() {
    String debitCardId = "1234";
    String accountId = "1111";
    DebitCard updatedDebitCard = DebitCard.builder()
        .id(debitCardId)
        .customerId("5678")
        .cardNumber("1234-5678-9012-3456")
        .cardholderName("John Doe")
        .expirationDate("01/23")
        .cvv("123")
        .mainAccountId("9876")
        .linkedAccounts(Set.of("2222"))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .status("active")
        .build();

    Mockito.when(service.removeLinkedAccount(debitCardId, accountId))
        .thenReturn(Mono.just(updatedDebitCard));

    client.delete().uri(BASE_URL + "/" + debitCardId + "/accounts/" + accountId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.id").isEqualTo(debitCardId)
        .jsonPath("$.linkedAccounts").isArray()
        .jsonPath("$.linkedAccounts").value(hasItem("2222"));

    Mockito.verify(service, Mockito.times(1)).removeLinkedAccount(debitCardId, accountId);
  }

}