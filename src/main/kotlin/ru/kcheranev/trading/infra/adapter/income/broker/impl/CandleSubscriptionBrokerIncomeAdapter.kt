package ru.kcheranev.trading.infra.adapter.income.broker.impl

import org.slf4j.LoggerFactory
import ru.kcheranev.trading.core.port.income.marketdata.ProcessCandleUseCase
import ru.kcheranev.trading.core.port.income.marketdata.ProcessIncomeCandleCommand
import ru.kcheranev.trading.infra.adapter.income.broker.brokerIncomeAdapterMapper
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.core.stream.StreamProcessor

class CandleSubscriptionBrokerIncomeAdapter(
    private val processCandleUseCase: ProcessCandleUseCase
) : StreamProcessor<MarketDataResponse> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(response: MarketDataResponse) {
        if (response.hasCandle()) {
            val candle = brokerIncomeAdapterMapper.map(response.candle)
            log.info("New income candle $candle")
            processCandleUseCase.processIncomeCandle(
                ProcessIncomeCandleCommand(candle)
            )
        }
    }

}