package com.nttbank.microservices.debitcardservice.controller;

import com.nttbank.microservices.debitcardservice.mapper.DebitCardMapper;
import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import com.nttbank.microservices.debitcardservice.model.record.DebitCardRecord;
import com.nttbank.microservices.debitcardservice.service.DebitCardService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * DebitCardController class provides the RESTful API endpoints for the Debit Card Service.
 */
@RestController
@RequestMapping("/debitcards")
@RequiredArgsConstructor
public class DebitCardController {

  private final DebitCardService service;
  private final DebitCardMapper mapper;

  /**
   * Retrieves all debit cards.
   *
   * @return a {@link Mono} containing a {@link ResponseEntity} with a {@link Flux} of debit cards.
   */
  @GetMapping
  public Mono<ResponseEntity<Flux<DebitCard>>> findAll() {
    return Mono.just(
            ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(service.findAll()))
        .defaultIfEmpty(ResponseEntity.noContent().build());
  }

  /**
   * Saves a new debit card.
   *
   * @param cardRecord the debit card record to save.
   * @return a {@link Mono} containing a {@link ResponseEntity} with the saved debit card.
   */
  @PostMapping
  public Mono<ResponseEntity<DebitCard>> save(@Valid @RequestBody DebitCardRecord cardRecord,
      final ServerHttpRequest req) {
    DebitCard debitCard = mapper.debitCardRecordToDebitCard(cardRecord);
    return service.save(debitCard)
        .map(c -> ResponseEntity.created(
                URI.create(req.getURI().toString().concat("/").concat(c.getId())))
            .contentType(MediaType.APPLICATION_JSON).body(c))
        .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
  }

  /**
   * Add a bank account to a debit card.
   *
   * @param debitCardId the ID of the debit card.
   * @param accountId   the ID of the bank account to link.
   * @return a {@link Mono} containing a {@link ResponseEntity} with the updated debit card.
   */
  @PostMapping("/{debit_card_id}/accounts/{account_id}")
  public Mono<ResponseEntity<DebitCard>> addLinkedAccount(
      @Valid @PathVariable("debit_card_id") String debitCardId,
      @Valid @PathVariable("account_id") String accountId) {

    return service.addLinkedAccount(debitCardId, accountId)
        .map(updatedCard -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
            .body(updatedCard))
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  /**
   * Remove a linked bank account from a debit card.
   *
   * @param debitCardId the ID of the debit card.
   * @param accountId   the ID of the bank account to unlink.
   * @return a {@link Mono} containing a {@link ResponseEntity} with the updated debit card.
   */
  @DeleteMapping("/{debit_card_id}/accounts/{account_id}")
  public Mono<ResponseEntity<DebitCard>> removeLinkedAccount(
      @Valid @PathVariable("debit_card_id") String debitCardId,
      @Valid @PathVariable("account_id") String accountId) {

    return service.removeLinkedAccount(debitCardId, accountId)
        .map(updatedCard -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
            .body(updatedCard))
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

}
