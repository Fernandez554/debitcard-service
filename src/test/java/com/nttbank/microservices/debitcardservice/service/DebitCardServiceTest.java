package com.nttbank.microservices.debitcardservice.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.nttbank.microservices.commonlibrary.event.GenericEvent;
import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import com.nttbank.microservices.debitcardservice.service.impl.DebitCardImpl;
import com.nttbank.microservices.debitcardservice.service.repository.IDebitCardRepo;
import com.nttbank.microservices.debitcardservice.util.KafkaUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;
import reactor.test.StepVerifier;

public class DebitCardServiceTest {

  @Mock
  private IDebitCardRepo repo;

  @Mock
  private KafkaUtil kafkaUtil;

  @InjectMocks
  private DebitCardImpl debitCardService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testFindAll() {
    when(repo.findAll()).thenReturn(Flux.just(new DebitCard()));
    Flux<DebitCard> result = debitCardService.findAll();
    assertNotNull(result);
  }

  @Test
  public void testFindById() {
    String id = UUID.randomUUID().toString();
    when(repo.findById(id)).thenReturn(Mono.just(new DebitCard()));
    Mono<DebitCard> result = debitCardService.findById(id);
    assertNotNull(result);
  }

  @Test
  public void testSave() {
    DebitCard debitCard = new DebitCard();
    when(repo.save(debitCard)).thenReturn(Mono.just(debitCard));
    Mono<DebitCard> result = debitCardService.save(debitCard);
    assertNotNull(result);
  }

  @Test
  public void testUpdate() {
    DebitCard debitCard = new DebitCard();
    when(repo.save(debitCard)).thenReturn(Mono.just(debitCard));
    Mono<DebitCard> result = debitCardService.update(debitCard);
    assertNotNull(result);
  }

  @Test
  public void testDelete() {
    String id = UUID.randomUUID().toString();
    when(repo.deleteById(id)).thenReturn(Mono.empty());
    Mono<Void> result = debitCardService.delete(id);
    assertNotNull(result);
  }

  @Test
  public void testAddLinkedAccount() {
    String debitCardId = UUID.randomUUID().toString();
    String accountId = UUID.randomUUID().toString();
    DebitCard debitCard = new DebitCard();

    // Mock repository behavior for a found debit card
    when(repo.findById(debitCardId)).thenReturn(Mono.just(debitCard));
    when(repo.save(debitCard)).thenReturn(Mono.just(debitCard));

    // Test case where the debit card is found and linked account is added
    StepVerifier.create(debitCardService.addLinkedAccount(debitCardId, accountId))
        .expectNext(debitCard)
        .verifyComplete();

    verify(repo, times(1)).findById(debitCardId);
    verify(repo, times(1)).save(debitCard);

    // Mock repository behavior for a not found debit card
    when(repo.findById(debitCardId)).thenReturn(Mono.empty());

    // Test case where the debit card is not found
    StepVerifier.create(debitCardService.addLinkedAccount(debitCardId, accountId))
        .verifyComplete();

    verify(repo, times(2)).findById(debitCardId); // Called twice in total
  }

  @Test
  public void testRemoveLinkedAccount() {
    String debitCardId = UUID.randomUUID().toString();
    String accountId = UUID.randomUUID().toString();
    DebitCard debitCard = new DebitCard();

    // Mock repository behavior for a found debit card
    when(repo.findById(debitCardId)).thenReturn(Mono.just(debitCard));
    when(repo.save(debitCard)).thenReturn(Mono.just(debitCard));

    // Test case where the debit card is found and  account is removed
    StepVerifier.create(debitCardService.removeLinkedAccount(debitCardId, accountId))
        .expectNext(debitCard)
        .verifyComplete();

    verify(repo, times(1)).findById(debitCardId);
    verify(repo, times(1)).save(debitCard);

    // Mock repository behavior for a not found debit card
    when(repo.findById(debitCardId)).thenReturn(Mono.empty());

    // Test case where the debit card is not found
    StepVerifier.create(debitCardService.removeLinkedAccount(debitCardId, accountId))
        .verifyComplete();

    verify(repo, times(2)).findById(debitCardId); // Called twice in total
  }


  @Test
  public void testFindByCardNumber() {
    String cardNumber = "1234567890123456";
    when(repo.findByCardNumber(cardNumber)).thenReturn(Mono.just(new DebitCard()));
    Mono<DebitCard> result = debitCardService.findByCardNumber(cardNumber);
    assertNotNull(result);
  }

  @Test
  public void testSendKafkaMessage() {
    GenericEvent event = new GenericEvent();
    doNothing().when(kafkaUtil).sendMessage(event);
    Mono<Void> result = debitCardService.sendKafkaMessage(event);
    assertNotNull(result);
  }
}