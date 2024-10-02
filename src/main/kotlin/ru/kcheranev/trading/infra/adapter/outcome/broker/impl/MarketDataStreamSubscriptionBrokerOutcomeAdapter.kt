package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.port.income.marketdata.ProcessCandleUseCase
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import ru.kcheranev.trading.domain.model.subscription.CandleSubscription
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

@Component
class MarketDataStreamSubscriptionBrokerOutcomeAdapter(
    private val marketDataStreamService: MarketDataStreamService,
    private val processCandleUseCase: ProcessCandleUseCase,
    private val candleSubscriptionHolder: CandleSubscriptionHolder
) : MarketDataStreamSubscriptionBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val lock = ReentrantLock()

    override fun subscribeCandles(command: SubscribeCandlesOrderCommand): Unit =
        lock.withLock {
            val instrument = command.instrument
            val candleInterval = command.candleInterval
            val subscriptionId = CandleSubscription.candleSubscriptionId(instrument, candleInterval)
            if (!candleSubscriptionHolder.checkSubscriptionExists(subscriptionId)) {
                log.info("Activate subscription for the trade session ticker=${instrument.ticker}, candleInterval=$candleInterval")
                marketDataStreamService.newStream(
                    CandleSubscription.candleSubscriptionId(instrument, candleInterval),
                    CandleSubscriptionBrokerIncomeAdapter(processCandleUseCase)
                ) { log.error(it.toString()) }
                    .subscribeCandlesWithWaitingClose(
                        listOf(command.instrument.id),
                        brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval)
                    )
            }
            candleSubscriptionHolder.incrementSubscriptionCount(instrument, candleInterval)
        }


    override fun unsubscribeCandles(command: UnsubscribeCandlesOrderCommand): Unit =
        lock.withLock {
            val instrument = command.instrument
            val candleInterval = command.candleInterval
            val subscriptionId = CandleSubscription.candleSubscriptionId(instrument, candleInterval)
            if (!candleSubscriptionHolder.checkSubscriptionExists(subscriptionId)) {
                return
            }
            if (candleSubscriptionHolder.isLastSubscription(subscriptionId)) {
                log.info("Deactivate subscription for the trade session ticker=${instrument.ticker}, candleInterval=$candleInterval")
                marketDataStreamService.getStreamById(subscriptionId)
                    .unsubscribeCandles(
                        listOf(command.instrument.id),
                        brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval)
                    )
            }
            candleSubscriptionHolder.removeCandleSubscription(subscriptionId)
        }

    override fun findAllCandleSubscriptions() = candleSubscriptionHolder.getSubscriptions()

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