package ru.kcheranev.trading.infra.adapter.income.broker

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.mapper.CommonBrokerMapper
import ru.kcheranev.trading.infra.adapter.mapper.commonBrokerMapper
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

@Mapper(uses = [CommonBrokerMapper::class])
interface BrokerIncomeAdapterMapper {

    fun map(source: ru.tinkoff.piapi.contract.v1.Candle): Candle {
        val candleInterval = map(source.interval)
        return Candle(
            interval = candleInterval,
            openPrice = commonBrokerMapper.map(source.open),
            closePrice = commonBrokerMapper.map(source.close),
            highestPrice = commonBrokerMapper.map(source.high),
            lowestPrice = commonBrokerMapper.map(source.low),
            volume = source.volume,
            endTime = commonBrokerMapper.map(source.time).plus(candleInterval.duration)
        )
    }

    fun map(source: SubscriptionInterval): CandleInterval =
        when (source) {
            SubscriptionInterval.SUBSCRIPTION_INTERVAL_UNSPECIFIED -> CandleInterval.UNDEFINED
            SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE -> CandleInterval.ONE_MIN
            SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIVE_MINUTES -> CandleInterval.FIVE_MIN
            SubscriptionInterval.UNRECOGNIZED -> CandleInterval.UNDEFINED
        }

}

val brokerIncomeAdapterMapper: BrokerIncomeAdapterMapper = Mappers.getMapper(BrokerIncomeAdapterMapper::class.java)