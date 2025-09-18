package com.github.trading.infra.adapter.mapper

import com.github.trading.common.date.mskZoneId
import com.google.protobuf.Timestamp
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.tinkoff.piapi.contract.v1.MoneyValue
import ru.tinkoff.piapi.contract.v1.Quotation
import ru.tinkoff.piapi.core.utils.MapperUtils
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime

@Mapper
abstract class CommonBrokerMapper {

    fun map(source: Quotation): BigDecimal = MapperUtils.quotationToBigDecimal(source)

    fun map(source: MoneyValue): BigDecimal = MapperUtils.moneyValueToBigDecimal(source)

    fun map(source: Timestamp): LocalDateTime =
        Instant.ofEpochSecond(source.seconds, source.nanos.toLong())
            .atZone(mskZoneId)
            .toLocalDateTime()

}

val commonBrokerMapper: CommonBrokerMapper = Mappers.getMapper(CommonBrokerMapper::class.java)