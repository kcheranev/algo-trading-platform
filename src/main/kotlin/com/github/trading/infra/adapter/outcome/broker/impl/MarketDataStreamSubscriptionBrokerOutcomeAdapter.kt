package com.github.trading.infra.adapter.outcome.broker.impl

import com.github.trading.core.port.income.marketdata.ProcessCandleUseCase
import com.github.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import com.github.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import com.github.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import com.github.trading.core.port.outcome.persistence.tradesession.IsReadyToOrderTradeSessionExistsCommand
import com.github.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import com.github.trading.domain.model.subscription.CandleSubscription
import com.github.trading.infra.adapter.income.broker.impl.CandleSubscriptionBrokerIncomeAdapter
import com.github.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
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
    private val brokerMarketDataStreamService: MarketDataStreamService,
    private val processCandleUseCase: ProcessCandleUseCase,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder
) : MarketDataStreamSubscriptionBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val lock = ReentrantLock()

    override fun subscribeCandles(command: SubscribeCandlesOrderCommand) {
        lock.withLock {
            val (instrument, candleInterval) = command
            val candleSubscription = CandleSubscription(instrument, candleInterval)
            if (!candleSubscriptionCacheHolder.contains(candleSubscription)) {
                log.info("Activate subscription for the trade session, ticker=${instrument.ticker}, candleInterval=$candleInterval")
                brokerMarketDataStreamService.newStream(
                    candleSubscription.id,
                    CandleSubscriptionBrokerIncomeAdapter(processCandleUseCase)
                ) { ex -> log.error(ex.toString()) }
                    .subscribeCandlesWithWaitingClose(
                        listOf(instrument.id),
                        brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval)
                    )
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
                brokerMarketDataStreamService.getStreamById(candleSubscription.id)
                    .unsubscribeCandles(
                        listOf(command.instrument.id),
                        brokerOutcomeAdapterMapper.mapToSubscriptionInterval(candleInterval)
                    )
                candleSubscriptionCacheHolder.remove(candleSubscription)
            }
        }
    }

    override fun findAllCandleSubscriptions(): Set<CandleSubscription> = candleSubscriptionCacheHolder.findAll()

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