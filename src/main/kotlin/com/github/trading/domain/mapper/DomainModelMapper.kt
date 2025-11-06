package com.github.trading.domain.mapper

import com.github.trading.common.date.toMskInstant
import com.github.trading.domain.model.Candle
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import org.ta4j.core.Bar
import org.ta4j.core.BarBuilder

@Mapper
abstract class DomainModelMapper {

    fun map(candle: Candle, barBuilder: BarBuilder): Bar {
        return with(candle) {
            barBuilder.timePeriod(interval.duration)
                .endTime(endDateTime.toMskInstant())
                .openPrice(openPrice)
                .highPrice(highestPrice)
                .lowPrice(lowestPrice)
                .closePrice(closePrice)
                .volume(volume)
                .build()
        }
    }

}

val domainModelMapper: DomainModelMapper = Mappers.getMapper(DomainModelMapper::class.java)