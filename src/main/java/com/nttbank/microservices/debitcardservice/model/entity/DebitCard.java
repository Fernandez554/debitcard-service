package com.nttbank.microservices.debitcardservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * DebitCard class represents the debit card entity.
 */
@Data
@Document("debit_cards")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class DebitCard {

  @EqualsAndHashCode.Include
  @Id
  private String id;
  private String customerId;
  private String cardNumber;
  private String cardholderName;
  private String expirationDate;
  private String cvv;
  private String mainAccountId;
  private Set<String> linkedAccounts;
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
  @Builder.Default
  private LocalDateTime updatedAt = LocalDateTime.now();
  @Builder.Default
  private String status = "active";

}
