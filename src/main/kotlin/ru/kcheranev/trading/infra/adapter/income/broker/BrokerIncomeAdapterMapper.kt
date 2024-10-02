package ru.kcheranev.trading.infra.adapter.income.broker

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.mapper.commonBrokerMapper
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

@Mapper
abstract class BrokerIncomeAdapterMapper {

    fun map(source: ru.tinkoff.piapi.contract.v1.Candle): Candle =
        with(source) {
            val candleInterval = map(interval)
            return Candle(
                interval = candleInterval,
                openPrice = commonBrokerMapper.map(open),
                closePrice = commonBrokerMapper.map(close),
                highestPrice = commonBrokerMapper.map(high),
                lowestPrice = commonBrokerMapper.map(low),
                volume = volume,
                endDateTime = commonBrokerMapper.map(time) + candleInterval.duration,
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