package com.nttbank.microservices.debitcardservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttbank.microservices.debitcardservice.model.response.AccountTransactionsResponse;
import com.nttbank.microservices.debitcardservice.model.response.BankAccountResponse;
import com.nttbank.microservices.debitcardservice.proxy.feign.CloudGatewayFeign;
import feign.FeignException;
import java.io.IOException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Service class responsible for handling account-related operations. This service interacts with a
 * Feign client to retrieve account data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

  private final CloudGatewayFeign feignAccount;

  /**
   * Tranfer money between two accounts.
   *
   * @param fromAccountId the ID of the account to transfer money from.
   * @param toAccountId   the ID of the account to transfer money to.
   * @param amount        the amount of money to transfer.
   * @return a {@link Mono} containing the account transactions response.
   */
  public Mono<AccountTransactionsResponse> accountTransfer(String fromAccountId, String toAccountId,
      BigDecimal amount) {
    return feignAccount.accountTransfer(fromAccountId, toAccountId, amount)
        .onErrorResume(e -> {
          if (e instanceof FeignException feignException) {
            String errorMessage = extractMessageFromFeignException(feignException);
            log.error("Error performing account transfer operation: {}", errorMessage);
            return Mono.error(new IllegalArgumentException(errorMessage));
          }
          return Mono.error(
              new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e));
        });
  }

  /**
   * Extracts the error message from a Feign exception.
   *
   * @param feignException the Feign exception to extract the message from.
   * @return the error message.
   */
  public String extractMessageFromFeignException(FeignException feignException) {
    try {
      String responseBody = feignException.contentUTF8();
      JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
      return jsonNode.path("message").asText("An error occurred");
    } catch (IOException ioException) {
      return "An error occurred";
    }
  }

  /**
   * Retrieves a bank account by its ID.
   *
   * @param accountId the ID of the account to retrieve.
   * @return a {@link Mono} containing the bank account response.
   */
  public Mono<BankAccountResponse> findById(String accountId) {
    return feignAccount.findById(accountId)
        .onErrorResume(ex -> {
          if (ex instanceof FeignException
              && ((FeignException) ex).status() == HttpStatus.NOT_FOUND.value()) {
            log.error("Account with ID {} not found: {}", accountId, ex.getMessage());
            return Mono.empty();
          }
          return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              "An unexpected error occurred", ex));
        });
  }
}
