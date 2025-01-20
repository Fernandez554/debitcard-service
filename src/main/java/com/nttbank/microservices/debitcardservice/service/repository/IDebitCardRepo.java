package com.nttbank.microservices.debitcardservice.service.repository;

import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * IDebitCardRepo interface provides the repository for the Debit Card entity.
 */
public interface IDebitCardRepo extends ReactiveMongoRepository<DebitCard, String> {

  /**
   * findByCardNumber method finds the debit card by card number.
   *
   * @param cardNumber the card number
   * @return the debit card
   */
  Mono<DebitCard> findByCardNumber(String cardNumber);

}
