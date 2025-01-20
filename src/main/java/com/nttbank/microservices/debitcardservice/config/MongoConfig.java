package com.nttbank.microservices.debitcardservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.transaction.reactive.TransactionalOperator;

/**
 * Configuration class for MongoDB. This configuration is responsible for customizing the MongoDB
 * converter by removing the default MongoDB type mapper, which adds a `_class` field to each
 * document.
 */
@Configuration
@RequiredArgsConstructor
public class MongoConfig implements InitializingBean {

  @Lazy
  private final MappingMongoConverter converter;

  @Override
  public void afterPropertiesSet() throws Exception {
    converter.setTypeMapper(new DefaultMongoTypeMapper(null));
  }

  /**
   * Creates and configures the transaction manager for MongoDB.
   */
  @Bean
  public ReactiveMongoTransactionManager transactionManager(ReactiveMongoDatabaseFactory factory) {
    return new ReactiveMongoTransactionManager(factory);
  }

  /**
   * Creates and configures the transaction operator for MongoDB.
   */
  @Bean
  public TransactionalOperator transactionalOperator(
      ReactiveMongoTransactionManager transactionManager) {
    return TransactionalOperator.create(transactionManager);
  }
}
