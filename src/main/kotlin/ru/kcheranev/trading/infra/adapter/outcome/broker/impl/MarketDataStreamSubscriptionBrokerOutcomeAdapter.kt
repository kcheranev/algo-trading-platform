package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.port.income.trading.ReceiveCandleUseCase
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.income.broker.impl.CandleSubscriptionBrokerIncomeAdapter
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.kcheranev.trading.infra.config.BrokerApi
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val CANDLES_STREAM_ID_FORMAT = "candles_%s_%s"

@Component
class MarketDataStreamSubscriptionBrokerOutcomeAdapter(
    brokerApi: BrokerApi,
    private val receiveCandleUseCase: ReceiveCandleUseCase
) : MarketDataStreamSubscriptionBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val marketDataStreamService = brokerApi.marketDataStreamService

    private val candleSubscriptions = mutableMapOf<String, Int>()

    private val lock = ReentrantLock()

    override fun subscribeCandles(command: SubscribeCandlesOrderCommand): Unit =
        lock.withLock {
            val ticker = command.instrument.ticker
            val candleInterval = command.candleInterval
            if (checkCandlesSubscriptionExists(ticker, candleInterval)) {
                return
            }
            log.info("Activate subscription for the $ticker $candleInterval")
            val candlesStreamId = CANDLES_STREAM_ID_FORMAT.format(ticker, candleInterval)
            marketDataStreamService.newStream(
                candlesStreamId,
                CandleSubscriptionBrokerIncomeAdapter(receiveCandleUseCase)
            ) { log.error(it.toString()) }
                .subscribeCandles(
                    listOf(command.instrument.id),
                    brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval)
                )
            candleSubscriptions.merge(candlesStreamId, 1) { oldValue, value -> oldValue + value }
        }


    override fun unsubscribeCandles(command: UnsubscribeCandlesOrderCommand): Unit =
        lock.withLock {
            val ticker = command.instrument.ticker
            val candleInterval = command.candleInterval
            if (!checkCandlesSubscriptionExists(ticker, candleInterval)) {
                return
            }
            log.info("Deactivate subscription for the $ticker $candleInterval")
            val candlesStreamId = CANDLES_STREAM_ID_FORMAT.format(ticker, candleInterval)
            marketDataStreamService.getStreamById(candlesStreamId)
                .unsubscribeCandles(
                    listOf(command.instrument.id),
                    brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval)
                )
            candleSubscriptions.compute(candlesStreamId) { _, value -> if (value == 1) null else value?.minus(1) }
        }

    private fun checkCandlesSubscriptionExists(ticker: String, candleInterval: CandleInterval): Boolean =
        candleSubscriptions.filter {
            it.key == CANDLES_STREAM_ID_FORMAT.format(ticker, candleInterval)
        }.isNotEmpty()

}