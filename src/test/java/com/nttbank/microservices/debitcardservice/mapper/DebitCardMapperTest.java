package com.nttbank.microservices.debitcardservice.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import com.nttbank.microservices.debitcardservice.model.record.DebitCardRecord;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class DebitCardMapperTest {

  @Test
  void debitCardRecordToDebitCard_GivenNullShouldReturnNull() {

    DebitCard debitCard = DebitCardMapper.INSTANCE.debitCardRecordToDebitCard(null);
    assertNull(debitCard);

  }

  @Test
  void testDebitCardRecordToDebitCard() {
    // Create a sample DebitCardRecord
    DebitCardRecord record = new DebitCardRecord(
        "dummy-id",
        "dummy-customer-id",
        "1234567890123456",
        "John Doe",
        "12/25",
        "123",
        "dummy-main-account-id",
        Set.of("linked-account-1", "linked-account-2")
    );

    // Map to DebitCard
    DebitCard debitCard = DebitCardMapper.INSTANCE.debitCardRecordToDebitCard(record);

    // Assert the mapping results
    assertNotNull(debitCard);
    assertEquals(debitCard.getId(), record.id());

    // Create a sample DebitCardRecord with null linkedAccounts
    DebitCardRecord record2 = new DebitCardRecord(
        "dummy-id",
        "dummy-customer-id",
        "1234567890123456",
        "John Doe",
        "12/25",
        "123",
        "dummy-main-account-id",
        null
    );
    // Map to DebitCard
    DebitCard debitCard2 = DebitCardMapper.INSTANCE.debitCardRecordToDebitCard(record2);

    // Assert the mapping results
    assertNotNull(debitCard2);
    assertEquals(debitCard2.getId(), record2.id());


  }
}
