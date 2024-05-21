package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.port.income.trading.ReceiveCandleUseCase
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import ru.kcheranev.trading.infra.adapter.income.broker.impl.CandleSubscriptionBrokerIncomeAdapter
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.tinkoff.piapi.contract.v1.CandleInstrument
import ru.tinkoff.piapi.contract.v1.MarketDataRequest
import ru.tinkoff.piapi.contract.v1.SubscribeCandlesRequest
import ru.tinkoff.piapi.contract.v1.SubscriptionAction
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.tinkoff.piapi.core.stream.MarketDataStreamService
import ru.tinkoff.piapi.core.stream.MarketDataSubscriptionService
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val CANDLES_STREAM_ID_FORMAT = "candles_%s_%s"

@Component
class MarketDataStreamSubscriptionBrokerOutcomeAdapter(
    private val marketDataStreamService: MarketDataStreamService,
    private val receiveCandleUseCase: ReceiveCandleUseCase,
    private val candleSubscriptionCounter: CandleSubscriptionCounter
) : MarketDataStreamSubscriptionBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val lock = ReentrantLock()

    override fun subscribeCandles(command: SubscribeCandlesOrderCommand): Unit =
        lock.withLock {
            val ticker = command.instrument.ticker
            val candleInterval = command.candleInterval
            val candlesStreamId = CANDLES_STREAM_ID_FORMAT.format(ticker, candleInterval)
            if (!candleSubscriptionCounter.checkSubscriptionExists(candlesStreamId)) {
                log.info("Activate subscription for the trade session ticker=$ticker, candleInterval=$candleInterval")
                marketDataStreamService.newStream(
                    candlesStreamId,
                    CandleSubscriptionBrokerIncomeAdapter(receiveCandleUseCase)
                ) { log.error(it.toString()) }
                    .subscribeCandlesWithWaitingClose(
                        listOf(command.instrument.id),
                        brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval)
                    )
            }
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
            if (candleSubscriptionCounter.lastSubscription(candlesStreamId)) {
                log.info("Deactivate subscription for the trade session ticker=$ticker, candleInterval=$candleInterval")
                marketDataStreamService.getStreamById(candlesStreamId)
                    .unsubscribeCandles(
                        listOf(command.instrument.id),
                        brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval)
                    )
            }
            candleSubscriptionCounter.removeCandleSubscription(candlesStreamId)
        }

}

@Suppress("UNCHECKED_CAST")
fun MarketDataSubscriptionService.subscribeCandlesWithWaitingClose(
    instrumentIds: List<String>,
    interval: SubscriptionInterval
) {
    val loadedClass = MarketDataSubscriptionService::class
    val observerField = loadedClass.java.getDeclaredField("observer")
    observerField.isAccessible = true
    val observerValue = observerField.get(this) as StreamObserver<MarketDataRequest>
    val builder =
        SubscribeCandlesRequest
            .newBuilder()
            .setSubscriptionAction(SubscriptionAction.SUBSCRIPTION_ACTION_SUBSCRIBE)
            .setWaitingClose(true)
    for (instrumentId in instrumentIds) {
        builder.addInstruments(
            CandleInstrument
                .newBuilder()
                .setInterval(interval)
                .setInstrumentId(instrumentId)
                .build()
        )
    }
    val request =
        MarketDataRequest
            .newBuilder()
            .setSubscribeCandlesRequest(builder)
            .build()
    observerValue.onNext(request)
}