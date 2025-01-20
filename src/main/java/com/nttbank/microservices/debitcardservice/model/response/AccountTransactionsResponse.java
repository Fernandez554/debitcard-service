package com.nttbank.microservices.debitcardservice.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * AccountTransactionsResponse class represents the account transactions response.
 */
@Data
@Builder
public class AccountTransactionsResponse {

  private String id;

  private String customerId;

  private String accountId;

  private String productName;

  private String type;

  private BigDecimal amount;

  private BigDecimal balanceAfterMovement;

  private LocalDateTime createdAt;

  private String description;

}
