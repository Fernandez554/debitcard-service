package com.nttbank.microservices.debitcardservice.config;

import com.nttbank.microservices.commonlibrary.event.DebitCardTransactionEvent;
import com.nttbank.microservices.commonlibrary.event.GenericEvent;
import com.nttbank.microservices.commonlibrary.event.TransferDebitCardEvent;
import com.nttbank.microservices.debitcardservice.service.AccountService;
import com.nttbank.microservices.debitcardservice.service.DebitCardService;
import com.nttbank.microservices.debitcardservice.util.KafkaUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.core.publisher.Mono;

/**
 * Configuration class for setting up Kafka consumer for the Debit Card Service.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

  private final DebitCardService service;
  private final AccountService accountService;
  private final KafkaUtil kafkaUtil;

  @Value("${kafka.nttbank.server:127.0.0.1}")
  private String kafkaServer;
  @Value("${kafka.nttbank.port:9092}")
  private String kafkaPort;
  @Value("${kafka.nttbank.topic.consumer:nttbank}")
  private String topicName;

  /**
   * Creates and configures the Kafka consumer factory.
   */
  @Bean
  public ConsumerFactory<String, GenericEvent<? extends GenericEvent>> consumerFactory() {
    Map<String, Object> kafkaProperties = new HashMap<>();
    kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer + ":" + kafkaPort);
    kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, topicName);

    kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        ErrorHandlingDeserializer.class);
    kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        ErrorHandlingDeserializer.class);

    kafkaProperties.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, JsonDeserializer.class);
    kafkaProperties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
        JsonDeserializer.class);

    kafkaProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "com.nttbank.microservices.*");

    return new DefaultKafkaConsumerFactory<>(kafkaProperties);
  }

  /**
   * Creates and configures the Kafka listener container factory.
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, GenericEvent<?
      extends GenericEvent>> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, GenericEvent<? extends GenericEvent>> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }

  /**
   * Listens to the Kafka topic and processes the messages.
   */
  @KafkaListener(topics = "debitcard-service-management-v1")
  public void listenTopic(GenericEvent<? extends GenericEvent> obj) {
    if (obj instanceof TransferDebitCardEvent transfer) {
      service.findByCardNumber(transfer.getDebitCardNumber())
          .flatMap(debitCard -> {
            if ("DEPOSIT_DEBIT_CARD".equals(transfer.getType())) {
              return accountService.accountTransfer(debitCard.getMainAccountId(),
                  transfer.getAccountId(),
                  transfer.getAmount());
            } else if ("WITHDRAW_DEBIT_CARD".equals(transfer.getType())) {
              return accountService.accountTransfer(transfer.getAccountId(),
                  debitCard.getMainAccountId(),
                  transfer.getAmount());
            } else {
              return Mono.error(
                  new IllegalArgumentException("Unknown transaction type: " + transfer.getType()));
            }
          })
          .switchIfEmpty(Mono.error(new IllegalArgumentException("Debit card not found")))
          .subscribe(
              response ->
                  service.sendKafkaMessage(DebitCardTransactionEvent.builder()
                      .transId(transfer.getTransactionId())
                      .type(transfer.getType())
                      .accountId(transfer.getAccountId())
                      .debitCardNumber(transfer.getDebitCardNumber())
                      .balanceUpdated(response.getBalanceAfterMovement())
                      .amount(transfer.getAmount())
                      .status("completed")
                      .description("transaction completed successfully")
                      .build()),
              error -> {
                service.sendKafkaMessage(DebitCardTransactionEvent.builder()
                    .transId(transfer.getTransactionId())
                    .status("error")
                    .type("TRANSACTION_FAILED")
                    .description(error.getMessage())
                    .build());
              }
          );
    }
  }

}
