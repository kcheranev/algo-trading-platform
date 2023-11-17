package ru.kcheranev.trading.infra.adapter.income.broker.impl

import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.core.stream.StreamProcessor

class CandleSubscriptionBrokerIncomeAdapter : StreamProcessor<MarketDataResponse> {

    override fun process(response: MarketDataResponse) {
        if (response.hasCandle()) {
            val candle = response.candle
        }
    }

}