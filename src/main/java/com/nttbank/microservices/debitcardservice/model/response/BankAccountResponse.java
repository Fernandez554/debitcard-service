package com.nttbank.microservices.debitcardservice.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a bank account response with various attributes including account type, customer ID,
 * balance, transaction limits, and other properties. This class is used to interact with the
 * 'account' collection in the MongoDB database.
 */
@Data
@Builder(toBuilder = true)
public class BankAccountResponse {

  private String id;

  private String accountType;

  private String customerId;

  private BigDecimal balance;

  private Integer maxMonthlyTrans;

  private BigDecimal maintenanceFee;

  private BigDecimal transactionFee;

  private Integer allowedDayOperation;

  private BigDecimal withdrawAmountMax;

  private Set<String> lstSigners;

  private Set<String> lstHolders;

  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  private LocalDateTime updatedAt = LocalDateTime.now();

  private String status;

}
