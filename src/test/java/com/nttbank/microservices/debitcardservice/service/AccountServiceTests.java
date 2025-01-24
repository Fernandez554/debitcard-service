package com.nttbank.microservices.debitcardservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttbank.microservices.debitcardservice.model.response.AccountTransactionsResponse;
import com.nttbank.microservices.debitcardservice.model.response.BankAccountResponse;
import com.nttbank.microservices.debitcardservice.proxy.feign.CloudGatewayFeign;
import feign.FeignException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class AccountServiceTests {

  @Mock
  private CloudGatewayFeign feignAccount;

  @InjectMocks
  private AccountService accountService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testAccountTransfer_Success() {
    String fromAccountId = "fromAccountId";
    String toAccountId = "toAccountId";
    BigDecimal amount = BigDecimal.valueOf(100);

    when(feignAccount.accountTransfer(fromAccountId, toAccountId, amount)).thenReturn(
        Mono.just(AccountTransactionsResponse.builder().build()));

    StepVerifier.create(accountService.accountTransfer(fromAccountId, toAccountId, amount))
        .expectNext(AccountTransactionsResponse.builder().build())
        .verifyComplete();

    verify(feignAccount, times(1)).accountTransfer(fromAccountId, toAccountId, amount);
  }

  @Test
  public void testAccountTransfer_Fail() {
    String fromAccountId = "fromAccountId";
    String toAccountId = "toAccountId";
    BigDecimal amount = BigDecimal.valueOf(100);
    FeignException feignException = mock(FeignException.class);

    when(feignAccount.accountTransfer(fromAccountId, toAccountId, amount)).thenReturn(
        Mono.error(feignException));
    when(feignException.contentUTF8()).thenReturn("{\"message\":\"Error occurred\"}");

    StepVerifier.create(accountService.accountTransfer(fromAccountId, toAccountId, amount))
        .expectError(IllegalArgumentException.class)
        .verify();

    verify(feignAccount, times(1)).accountTransfer(fromAccountId, toAccountId, amount);
  }

  @Test
  public void testExtractMessageFromFeignException_IOException() {
    FeignException feignException = mock(FeignException.class);
    when(feignException.contentUTF8()).thenReturn("invalid json");

    String result = accountService.extractMessageFromFeignException(feignException);

    assertEquals("An error occurred", result);
  }

  @Test
  public void testFindById_Success() {
    String accountId = "accountId";

    BankAccountResponse response = BankAccountResponse.builder().build();
    when(feignAccount.findById(accountId)).thenReturn(Mono.just(response));

    StepVerifier.create(accountService.findById(accountId))
        .expectNext(response)
        .verifyComplete();

    verify(feignAccount, times(1)).findById(accountId);
  }

  @Test
  public void testFindById_NotFound() {
    String accountId = "accountId";
    FeignException feignException = mock(FeignException.class);

    when(feignAccount.findById(accountId)).thenReturn(Mono.error(feignException));
    when(feignException.status()).thenReturn(HttpStatus.NOT_FOUND.value());

    StepVerifier.create(accountService.findById(accountId))
        .verifyComplete();

    verify(feignAccount, times(1)).findById(accountId);
  }

  @Test
  public void testFindById_Fail() {
    String accountId = "accountId";
    FeignException feignException = mock(FeignException.class);

    when(feignAccount.findById(accountId)).thenReturn(Mono.error(feignException));
    when(feignException.status()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());

    StepVerifier.create(accountService.findById(accountId))
        .expectError(ResponseStatusException.class)
        .verify();

    verify(feignAccount, times(1)).findById(accountId);
  }
}
