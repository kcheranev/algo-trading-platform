package com.github.trading.infra.adapter.income.broker.impl

import com.github.trading.core.port.income.marketdata.ProcessCandleUseCase
import com.github.trading.core.port.income.marketdata.ProcessIncomeCandleCommand
import com.github.trading.infra.adapter.income.broker.brokerIncomeAdapterMapper
import org.slf4j.LoggerFactory
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