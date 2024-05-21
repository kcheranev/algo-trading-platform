package ru.kcheranev.trading.infra.adapter.income.broker.impl

import org.slf4j.LoggerFactory
import ru.kcheranev.trading.core.port.income.trading.ProcessIncomeCandleCommand
import ru.kcheranev.trading.core.port.income.trading.ReceiveCandleUseCase
import ru.kcheranev.trading.infra.adapter.income.broker.brokerIncomeAdapterMapper
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.core.stream.StreamProcessor

class CandleSubscriptionBrokerIncomeAdapter(
    private val receiveCandleUseCase: ReceiveCandleUseCase
) : StreamProcessor<MarketDataResponse> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(response: MarketDataResponse) {
        if (response.hasCandle()) {
            val candle = brokerIncomeAdapterMapper.map(response.candle)
            log.info("New income candle $candle")
            receiveCandleUseCase.processIncomeCandle(
                ProcessIncomeCandleCommand(candle)
            )
        }
    }

}