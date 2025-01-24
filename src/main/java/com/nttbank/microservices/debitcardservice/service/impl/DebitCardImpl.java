package com.nttbank.microservices.debitcardservice.service.impl;

import com.nttbank.microservices.commonlibrary.event.GenericEvent;
import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import com.nttbank.microservices.debitcardservice.service.DebitCardService;
import com.nttbank.microservices.debitcardservice.service.repository.IDebitCardRepo;
import com.nttbank.microservices.debitcardservice.util.DebitCardUtils;
import com.nttbank.microservices.debitcardservice.util.KafkaUtil;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * DebitCardImpl class provides the implementation of the Debit Card Service.
 */
@Service
@RequiredArgsConstructor
public class DebitCardImpl implements DebitCardService {

  private final IDebitCardRepo repo;
  private final KafkaUtil kafkaUtil;

  @Override
  public Flux<DebitCard> findAll() {
    return repo.findAll();
  }

  @Override
  public Mono<DebitCard> findById(String debitCardId) {
    return repo.findById(debitCardId);
  }

  @Override
  public Mono<DebitCard> save(DebitCard t) {
    t.setMainAccountId(generateFormattedUuid());
    return repo.save(t);
  }

  private String generateFormattedUuid() {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    return uuid.substring(0, Math.min(uuid.length(), 16));
  }

  @Override
  public Mono<DebitCard> update(DebitCard t) {
    return repo.save(t);
  }

  @Override
  public Mono<Void> delete(String debitCardId) {
    return repo.deleteById(debitCardId);
  }

  @Override
  public Mono<DebitCard> addLinkedAccount(String debitCardId, String accountId) {
    return repo.findById(debitCardId)
        .flatMap(debitCard -> {
          DebitCardUtils.addElementToSet(debitCard, accountId, DebitCard::getLinkedAccounts,
              DebitCard::setLinkedAccounts);
          return repo.save(debitCard);
        });
  }

  @Override
  public Mono<DebitCard> removeLinkedAccount(String debitCardId, String accountId) {
    return repo.findById(debitCardId)
        .flatMap(debitCard -> {
          DebitCardUtils.removeElementToSet(debitCard, accountId, DebitCard::getLinkedAccounts,
              DebitCard::setLinkedAccounts);
          return repo.save(debitCard);
        });
  }

  @Override
  public Mono<DebitCard> findByCardNumber(String cardNumber) {
    return repo.findByCardNumber(cardNumber);
  }

  @Override
  public Mono<Void> sendKafkaMessage(GenericEvent obj) {
    kafkaUtil.sendMessage(obj);
    return Mono.empty();
  }

}
