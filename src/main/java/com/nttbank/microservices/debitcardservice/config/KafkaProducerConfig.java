package com.nttbank.microservices.debitcardservice.config;

import com.nttbank.microservices.commonlibrary.event.GenericEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Configuration class for setting up Kafka producer for the Debit Card Service.
 */
@Configuration
public class KafkaProducerConfig {

  @Value("${kafka.nttbank.server:127.0.0.1}")
  private String kafkaServer;

  @Value("${kafka.nttbank.port:}")
  private String kafkaPort;

  /**
   * Creates and configures the Kafka producer factory.
   */
  public ProducerFactory<String, GenericEvent> producerFactory() {
    Map<String, Object> kafkaProperties = new HashMap<>();
    kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer + ":" + kafkaPort);
    kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(kafkaProperties);
  }

  /**
   * Creates and configures the Kafka template.
   */
  @Bean
  public KafkaTemplate<String, GenericEvent> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

}
