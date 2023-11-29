package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.common.LoggerDelegate
import ru.kcheranev.trading.core.port.income.ReceiveCandleUseCase
import ru.kcheranev.trading.core.port.outcome.broker.CheckCandlesSubscriptionExistsCommand
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import ru.kcheranev.trading.infra.adapter.income.broker.impl.CandleSubscriptionBrokerIncomeAdapter
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.kcheranev.trading.infra.config.BrokerApi
import java.util.concurrent.ConcurrentHashMap

@Component
class MarketDataStreamSubscriptionBrokerOutcomeAdapter(
    brokerApi: BrokerApi,
    private val receiveCandleUseCase: ReceiveCandleUseCase
) : MarketDataStreamSubscriptionBrokerPort {

    private val marketDataStreamService = brokerApi.marketDataStreamService

    private val candleSubscriptions = ConcurrentHashMap<String, Int>()

    override fun subscribeCandles(command: SubscribeCandlesOrderCommand) {
        logger.info("Activate subscription for the ${command.ticker} ${command.candleInterval}")
        val candlesStreamId = CANDLES_STREAM_ID_FORMAT.format(command.ticker, command.candleInterval)
        marketDataStreamService.newStream(
            candlesStreamId,
            CandleSubscriptionBrokerIncomeAdapter(receiveCandleUseCase)
        ) { logger.error(it.toString()) }
            .subscribeCandles(
                listOf(command.instrumentId),
                brokerOutcomeAdapterMapper.mapToSubscriptionInterval(command.candleInterval)
            )
        candleSubscriptions.merge(candlesStreamId, 1) { oldValue, value -> oldValue + value }
    }

    override fun unsubscribeCandles(command: UnsubscribeCandlesOrderCommand) {
        logger.info("Deactivate subscription for the ${command.ticker} ${command.candleInterval}")
        val candlesStreamId = CANDLES_STREAM_ID_FORMAT.format(command.ticker, command.candleInterval)
        marketDataStreamService.getStreamById(candlesStreamId)
            .unsubscribeCandles(
                listOf(command.instrumentId),
                brokerOutcomeAdapterMapper.mapToSubscriptionInterval(command.candleInterval)
            )
        candleSubscriptions.compute(candlesStreamId) { _, value -> if (value == 1) null else value?.minus(1) }
    }

    override fun checkCandlesSubscriptionExists(command: CheckCandlesSubscriptionExistsCommand): Boolean =
        candleSubscriptions.filter { it.key == CANDLES_STREAM_ID_FORMAT.format(command.ticker, command.candleInterval) }
            .isNotEmpty()

    companion object {

        private val logger by LoggerDelegate()

        private const val CANDLES_STREAM_ID_FORMAT = "candles_%s_%s"

    }

}