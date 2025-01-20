package com.nttbank.microservices.debitcardservice.mapper;

import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import com.nttbank.microservices.debitcardservice.model.record.DebitCardRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

/**
 * DebitCardMapper interface provides the mapping between DebitCard and DebitCardRecord objects.
 */
@Mapper(componentModel = ComponentModel.SPRING)
public interface DebitCardMapper {

  DebitCardMapper INSTANCE = Mappers.getMapper(DebitCardMapper.class);

  DebitCard debitCardRecordToDebitCard(DebitCardRecord debitCardRecord);

}
