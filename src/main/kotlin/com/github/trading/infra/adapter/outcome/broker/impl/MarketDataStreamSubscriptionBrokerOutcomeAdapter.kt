package com.github.trading.infra.adapter.outcome.broker.impl

import com.github.trading.core.port.income.marketdata.ProcessCandleUseCase
import com.github.trading.core.port.income.marketdata.ProcessIncomeCandleCommand
import com.github.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import com.github.trading.core.port.outcome.broker.SubscribeCandlesCommand
import com.github.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import com.github.trading.core.port.outcome.persistence.tradesession.IsReadyToOrderTradeSessionExistsCommand
import com.github.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import com.github.trading.domain.model.subscription.CandleSubscription
import com.github.trading.infra.adapter.income.broker.brokerIncomeAdapterMapper
import com.github.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.tinkoff.piapi.contract.v1.GetCandlesRequest.CandleSource
import ru.ttech.piapi.core.impl.marketdata.MarketDataStreamManager
import ru.ttech.piapi.core.impl.marketdata.subscription.CandleSubscriptionSpec
import ru.ttech.piapi.core.impl.marketdata.subscription.Instrument
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class MarketDataStreamSubscriptionBrokerOutcomeAdapter(
    private val marketDataStreamManager: MarketDataStreamManager,
    private val processCandleUseCase: ProcessCandleUseCase,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder
) : MarketDataStreamSubscriptionBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val lock = ReentrantLock()

    init {
        marketDataStreamManager.start()
    }

    override fun subscribeCandles(command: SubscribeCandlesCommand) {
        lock.withLock {
            val (instrument, candleInterval) = command
            val candleSubscription = CandleSubscription(instrument, candleInterval)
            if (!candleSubscriptionCacheHolder.contains(candleSubscription)) {
                log.info("Activate subscription for the trade session, ticker=${instrument.ticker}, candleInterval=$candleInterval")
                marketDataStreamManager.subscribeCandles(
                    setOf(Instrument(instrument.id, brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval))),
                    CandleSubscriptionSpec(CandleSource.CANDLE_SOURCE_EXCHANGE)
                ) { candleWrapper ->
                    processCandleUseCase.processIncomeCandle(
                        ProcessIncomeCandleCommand(brokerIncomeAdapterMapper.map(candleWrapper))
                    )
                }
                candleSubscriptionCacheHolder.add(candleSubscription)
            }
        }
    }

    override fun unsubscribeCandles(command: UnsubscribeCandlesOrderCommand) {
        lock.withLock {
            val (instrument, candleInterval) = command
            val candleSubscription = CandleSubscription(instrument, candleInterval)
            if (!candleSubscriptionCacheHolder.contains(candleSubscription)) {
                return
            }
            val isReadyForOrderTradeSessionExists =
                tradeSessionPersistencePort.isReadyForOrderTradeSessionExists(
                    IsReadyToOrderTradeSessionExistsCommand(instrument.id, candleInterval)
                )
            if (!isReadyForOrderTradeSessionExists) {
                log.info("Deactivate subscription for the trade session, ticker=${instrument.ticker}, candleInterval=$candleInterval")
                marketDataStreamManager.unsubscribeCandles(
                    setOf(Instrument(instrument.id, brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval))),
                    CandleSubscriptionSpec(CandleSource.CANDLE_SOURCE_EXCHANGE)
                )
                candleSubscriptionCacheHolder.remove(candleSubscription)
            }
        }
    }

    override fun findAllCandleSubscriptions(): Set<CandleSubscription> = candleSubscriptionCacheHolder.findAll()

}