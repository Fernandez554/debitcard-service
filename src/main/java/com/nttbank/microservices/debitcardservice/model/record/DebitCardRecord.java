package com.nttbank.microservices.debitcardservice.model.record;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Builder;

/**
 * DebitCardRecord class represents the debit card record.
 */
@Builder
public record DebitCardRecord(
    String id,

    @NotNull(message = "Customer ID cannot be null")
    @NotEmpty(message = "Customer ID cannot be empty")
    String customerId,

    String cardNumber,

    String cardholderName,

    String expirationDate,

    String cvv,

    @NotNull(message = "Main account ID cannot be null")
    @NotEmpty(message = "Main account ID cannot be empty")
    String mainAccountId,

    Set<String> linkedAccounts
) {

}