package com.github.trading.infra.adapter.income.broker

import com.github.trading.common.date.utcAsMskLocalDateTime
import com.github.trading.domain.model.Candle
import com.github.trading.domain.model.CandleInterval
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.ttech.piapi.core.impl.marketdata.wrapper.CandleWrapper

@Mapper
abstract class BrokerIncomeAdapterMapper {

    fun map(source: CandleWrapper): Candle =
        with(source) {
            val candleInterval = map(interval)
            return Candle(
                interval = candleInterval,
                openPrice = open,
                closePrice = close,
                highestPrice = high,
                lowestPrice = low,
                volume = volume,
                endDateTime = time.utcAsMskLocalDateTime() + candleInterval.duration,
                instrumentId = instrumentUid
            )
        }


    fun map(source: SubscriptionInterval) =
        when (source) {
            SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE -> CandleInterval.ONE_MIN
            SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIVE_MINUTES -> CandleInterval.FIVE_MIN
            else -> throw UnexpectedSubscriptionIntervalException(source)
        }

}

val brokerIncomeAdapterMapper: BrokerIncomeAdapterMapper = Mappers.getMapper(BrokerIncomeAdapterMapper::class.java)