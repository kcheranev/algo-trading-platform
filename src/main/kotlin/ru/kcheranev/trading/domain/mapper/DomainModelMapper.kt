package ru.kcheranev.trading.domain.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import org.ta4j.core.BaseBar
import ru.kcheranev.trading.common.date.toMskZonedDateTime
import ru.kcheranev.trading.domain.model.Candle
import java.math.BigDecimal

@Mapper
abstract class DomainModelMapper {

    fun map(candle: Candle): BaseBar {
        return with(candle) {
            BaseBar(
                interval.duration,
                endTime.toMskZonedDateTime(),
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