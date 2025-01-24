package com.nttbank.microservices.debitcardservice.config;

import com.nttbank.microservices.commonlibrary.event.DebitCardTransactionEvent;
import com.nttbank.microservices.commonlibrary.event.GenericEvent;
import com.nttbank.microservices.commonlibrary.event.TransferDebitCardEvent;
import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import com.nttbank.microservices.debitcardservice.model.response.AccountTransactionsResponse;
import com.nttbank.microservices.debitcardservice.service.AccountService;
import com.nttbank.microservices.debitcardservice.service.DebitCardService;
import com.nttbank.microservices.debitcardservice.util.KafkaUtil;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import java.util.Map;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaConsumerConfigTest {

  private KafkaConsumerConfig kafkaConsumerConfig;
  private DebitCardService debitCardService;
  private AccountService accountService;
  private KafkaUtil kafkaUtil;

  @BeforeEach
  void setUp() throws Exception {
    debitCardService = mock(DebitCardService.class);
    accountService = mock(AccountService.class);
    kafkaUtil = mock(KafkaUtil.class);
    kafkaConsumerConfig = new KafkaConsumerConfig(debitCardService, accountService, kafkaUtil);

    // Use reflection to set private fields
    setPrivateField(kafkaConsumerConfig, "kafkaServer", "localhost");
    setPrivateField(kafkaConsumerConfig, "kafkaPort", "9092");
    setPrivateField(kafkaConsumerConfig, "topicName", "nttbank");
  }

  private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void testConsumerFactory() {
    ConsumerFactory<String, GenericEvent<? extends GenericEvent>> consumerFactory =
        kafkaConsumerConfig.consumerFactory();
    assertNotNull(consumerFactory);
    assertInstanceOf(DefaultKafkaConsumerFactory.class, consumerFactory);

    Map<String, Object> configs =
        ((DefaultKafkaConsumerFactory) consumerFactory).getConfigurationProperties();
    assertEquals("localhost:9092", configs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
    assertEquals("nttbank", configs.get(ConsumerConfig.GROUP_ID_CONFIG));
    assertEquals(ErrorHandlingDeserializer.class,
        configs.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
    assertEquals(ErrorHandlingDeserializer.class,
        configs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
  }

  @Test
  void testKafkaListenerContainerFactory() {
    var factory = kafkaConsumerConfig.kafkaListenerContainerFactory();
    assertNotNull(factory);
    assertNotNull(factory.getConsumerFactory());
  }

  @Test
  void testListenTopic_withDepositTransferEvent() {
    TransferDebitCardEvent transferEvent = new TransferDebitCardEvent();
    transferEvent.setDebitCardNumber("1234");
    transferEvent.setType("DEPOSIT_DEBIT_CARD");
    transferEvent.setAccountId("2");
    transferEvent.setTransactionId("transId");
    transferEvent.setAmount(BigDecimal.ONE);

    DebitCard debitCard = DebitCard.builder().mainAccountId("1").build();
    when(debitCardService.findByCardNumber("1234")).thenReturn(Mono.just(debitCard));
    when(accountService.accountTransfer("1", "2", BigDecimal.ONE))
        .thenReturn(Mono.just(AccountTransactionsResponse.builder()
            .balanceAfterMovement(BigDecimal.TEN).build()));

    kafkaConsumerConfig.listenTopic(transferEvent);

    StepVerifier.create(debitCardService.findByCardNumber("1234"))
        .expectNext(debitCard)
        .verifyComplete();

    verify(accountService, times(1))
        .accountTransfer("1", "2", BigDecimal.ONE);

    ArgumentCaptor<DebitCardTransactionEvent> captor =
        ArgumentCaptor.forClass(DebitCardTransactionEvent.class);
    verify(debitCardService, times(1)).sendKafkaMessage(captor.capture());

    DebitCardTransactionEvent event = captor.getValue();
    assertEquals("transId", event.getTransId());
    assertEquals("completed", event.getStatus());
    assertEquals("transaction completed successfully", event.getDescription());
  }

  @Test
  void testListenTopic_withWithdrawTransferEvent() {
    TransferDebitCardEvent transferEvent = new TransferDebitCardEvent();
    transferEvent.setDebitCardNumber("1234");
    transferEvent.setType("WITHDRAW_DEBIT_CARD");
    transferEvent.setAccountId("1");
    transferEvent.setTransactionId("transId");
    transferEvent.setAmount(BigDecimal.ONE);

    DebitCard debitCard = DebitCard.builder().mainAccountId("2").build();
    when(debitCardService.findByCardNumber("1234")).thenReturn(Mono.just(debitCard));
    when(accountService.accountTransfer("1", "2", BigDecimal.ONE))
        .thenReturn(Mono.just(AccountTransactionsResponse.builder()
            .balanceAfterMovement(BigDecimal.TEN).build()));

    kafkaConsumerConfig.listenTopic(transferEvent);

    StepVerifier.create(debitCardService.findByCardNumber("1234"))
        .expectNext(debitCard)
        .verifyComplete();

    verify(accountService, times(1))
        .accountTransfer("1", "2", BigDecimal.ONE);

    ArgumentCaptor<DebitCardTransactionEvent> captor =
        ArgumentCaptor.forClass(DebitCardTransactionEvent.class);
    verify(debitCardService, times(1)).sendKafkaMessage(captor.capture());

    DebitCardTransactionEvent event = captor.getValue();
    assertEquals("transId", event.getTransId());
    assertEquals("completed", event.getStatus());
    assertEquals("transaction completed successfully", event.getDescription());
  }

  @Test
  void testListenTopic_withInvalidTransferEvent() {
    TransferDebitCardEvent transferEvent = new TransferDebitCardEvent();
    transferEvent.setDebitCardNumber("1234");
    transferEvent.setType("INVALID_TYPE");
    transferEvent.setAccountId("2");
    transferEvent.setTransactionId("transId");
    transferEvent.setAmount(BigDecimal.ONE);

    when(debitCardService.findByCardNumber("1234")).thenReturn(Mono.empty());

    kafkaConsumerConfig.listenTopic(transferEvent);

    verify(debitCardService, times(1)).findByCardNumber("1234");

    ArgumentCaptor<DebitCardTransactionEvent> captor =
        ArgumentCaptor.forClass(DebitCardTransactionEvent.class);
    verify(debitCardService, times(1)).sendKafkaMessage(captor.capture());

    DebitCardTransactionEvent event = captor.getValue();
    assertEquals("transId", event.getTransId());
    assertEquals("TRANSACTION_FAILED", event.getType());
    assertTrue(event.getDescription().contains("Debit card not found"));
  }

  @Test
  void testListenTopic_withInvalidEvent() {
    GenericEvent<?> invalidEvent = mock(GenericEvent.class);

    kafkaConsumerConfig.listenTopic((GenericEvent<? extends GenericEvent>) invalidEvent);

    // No interactions should occur for invalid event
    verifyNoInteractions(debitCardService);
    verifyNoInteractions(accountService);
  }
}