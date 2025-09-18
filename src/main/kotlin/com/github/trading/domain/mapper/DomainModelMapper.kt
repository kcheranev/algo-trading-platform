package com.github.trading.domain.mapper

import com.github.trading.common.date.toMskZonedDateTime
import com.github.trading.domain.model.Candle
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import org.ta4j.core.BaseBar
import java.math.BigDecimal

@Mapper
abstract class DomainModelMapper {

    fun map(candle: Candle): BaseBar {
        return with(candle) {
            BaseBar(
                interval.duration,
                endDateTime.toMskZonedDateTime(),
                openPrice,
                highestPrice,
                lowestPrice,
                closePrice,
                BigDecimal(volume)
            )
        }
    }

}

val domainModelMapper: DomainModelMapper = Mappers.getMapper(DomainModelMapper::class.java)