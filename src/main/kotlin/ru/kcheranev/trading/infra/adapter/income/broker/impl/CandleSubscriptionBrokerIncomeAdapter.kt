package ru.kcheranev.trading.infra.adapter.income.broker.impl

import ru.kcheranev.trading.common.LoggerDelegate
import ru.kcheranev.trading.core.port.income.trading.ProcessIncomeCandleCommand
import ru.kcheranev.trading.core.port.income.trading.ReceiveCandleUseCase
import ru.kcheranev.trading.infra.adapter.income.broker.brokerIncomeAdapterMapper
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.core.stream.StreamProcessor

class CandleSubscriptionBrokerIncomeAdapter(
    private val receiveCandleUseCase: ReceiveCandleUseCase
) : StreamProcessor<MarketDataResponse> {

    override fun process(response: MarketDataResponse) {
        if (response.hasCandle()) {
            val candle = response.candle
            logger.info("New income candle for the instrument ${candle.instrumentUid}")
            receiveCandleUseCase.processIncomeCandle(
                ProcessIncomeCandleCommand(brokerIncomeAdapterMapper.map(candle))
            )
        }
    }

    companion object {

        private val logger by LoggerDelegate()

    }

}