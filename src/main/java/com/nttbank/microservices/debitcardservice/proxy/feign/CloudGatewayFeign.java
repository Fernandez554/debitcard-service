package com.nttbank.microservices.debitcardservice.proxy.feign;

import com.nttbank.microservices.debitcardservice.model.response.AccountTransactionsResponse;
import com.nttbank.microservices.debitcardservice.model.response.BankAccountResponse;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

/**
 * Feign client interface for interacting with the external account service. This client allows
 * making reactive HTTP requests to retrieve account data.
 */
@ReactiveFeignClient(name = "cloud-gateway")
public interface CloudGatewayFeign {

  /**
   * Transfers money between two accounts.
   *
   * @param fromAccountId the ID of the account to transfer money from.
   * @param toAccountId   the ID of the account to transfer money to.
   * @param amount        the amount of money to transfer.
   * @return a {@link Mono} containing the account transactions response.
   */
  @PostMapping("/api/account-service/accounts/{from_account_id}/{to_account_id}/transfer")
  Mono<AccountTransactionsResponse> accountTransfer(
      @PathVariable("from_account_id") String fromAccountId,
      @PathVariable("to_account_id") String toAccountId,
      @RequestParam("amount") BigDecimal amount);

  /**
   * Retrieves a bank account by its ID.
   *
   * @param accountId the ID of the account to retrieve.
   * @return a {@link Mono} containing the bank account response.
   */
  @GetMapping("/api/account-service/accounts/{account_id}")
  Mono<BankAccountResponse> findById(@PathVariable("account_id") String accountId);

}
