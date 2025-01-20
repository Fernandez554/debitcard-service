package com.nttbank.microservices.debitcardservice.service;

import com.nttbank.microservices.commonlibrary.event.GenericEvent;
import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * DebitCardService interface provides the service for the Debit Card entity.
 */
public interface DebitCardService {

  /**
   * findAll method finds all the debit cards.
   *
   * @return the debit cards
   */
  Flux<DebitCard> findAll();

  /**
   * findById method finds the debit card by id.
   *
   * @param debitCardId the debit card id
   * @return the debit card
   */
  Mono<DebitCard> findById(String debitCardId);

  /**
   * save method saves the debit card.
   *
   * @param t the debit card
   * @return the saved debit card
   */
  Mono<DebitCard> save(DebitCard t);

  /**
   * update method updates the debit card.
   *
   * @param t the debit card
   * @return the updated debit card
   */
  Mono<DebitCard> update(DebitCard t);

  /**
   * delete method deletes the debit card.
   *
   * @param debitCardId the debit card id
   * @return the void
   */
  Mono<Void> delete(String debitCardId);

  /**
   * addLinkedAccount method adds a linked account to the debit card.
   *
   * @param debitCardId the debit card id
   * @param accountId   the account id
   * @return the debit card
   */
  Mono<DebitCard> addLinkedAccount(String debitCardId, String accountId);

  /**
   * removeLinkedAccount method removes a linked account from the debit card.
   *
   * @param debitCardId the debit card id
   * @param accountId   the account id
   * @return the debit card
   */
  Mono<DebitCard> removeLinkedAccount(String debitCardId, String accountId);

  /**
   * findByCardNumber method finds the debit card by card number.
   *
   * @param cardNumber the card number
   * @return the debit card
   */
  Mono<DebitCard> findByCardNumber(String cardNumber);

  /**
   * sendKafkaMessage method sends a Kafka message.
   *
   * @param obj the generic event
   * @return the void
   */
  Mono<Void> sendKafkaMessage(GenericEvent obj);
}
