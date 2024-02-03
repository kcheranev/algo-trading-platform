package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.port.income.trading.ReceiveCandleUseCase
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import ru.kcheranev.trading.infra.adapter.income.broker.impl.CandleSubscriptionBrokerIncomeAdapter
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.kcheranev.trading.infra.config.BrokerApi
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val CANDLES_STREAM_ID_FORMAT = "candles_%s_%s"

@Component
class MarketDataStreamSubscriptionBrokerOutcomeAdapter(
    brokerApi: BrokerApi,
    private val receiveCandleUseCase: ReceiveCandleUseCase,
    private val candleSubscriptionCounter: CandleSubscriptionCounter
) : MarketDataStreamSubscriptionBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val marketDataStreamService = brokerApi.marketDataStreamService

    private val lock = ReentrantLock()

    override fun subscribeCandles(command: SubscribeCandlesOrderCommand): Unit =
        lock.withLock {
            val ticker = command.instrument.ticker
            val candleInterval = command.candleInterval
            val candlesStreamId = CANDLES_STREAM_ID_FORMAT.format(ticker, candleInterval)
            if (candleSubscriptionCounter.checkSubscriptionExists(candlesStreamId)) {
                return
            }
            log.info("Activate subscription for the trade session ticker=$ticker, candleInterval=$candleInterval")
            marketDataStreamService.newStream(
                candlesStreamId,
                CandleSubscriptionBrokerIncomeAdapter(receiveCandleUseCase)
            ) { log.error(it.toString()) }
                .subscribeCandles(
                    listOf(command.instrument.id),
                    brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval)
                )
            candleSubscriptionCounter.addCandleSubscription(candlesStreamId)
        }


    override fun unsubscribeCandles(command: UnsubscribeCandlesOrderCommand): Unit =
        lock.withLock {
            val ticker = command.instrument.ticker
            val candleInterval = command.candleInterval
            val candlesStreamId = CANDLES_STREAM_ID_FORMAT.format(ticker, candleInterval)
            if (!candleSubscriptionCounter.checkSubscriptionExists(candlesStreamId)) {
                return
            }
            log.info("Deactivate subscription for the trade session ticker=$ticker, candleInterval=$candleInterval")
            marketDataStreamService.getStreamById(candlesStreamId)
                .unsubscribeCandles(
                    listOf(command.instrument.id),
                    brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval)
                )
            candleSubscriptionCounter.removeCandleSubscription(candlesStreamId)
        }

}