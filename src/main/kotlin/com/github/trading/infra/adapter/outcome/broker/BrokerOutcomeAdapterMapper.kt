package com.github.trading.infra.adapter.outcome.broker

import com.github.trading.core.port.outcome.broker.model.BuyLimits
import com.github.trading.core.port.outcome.broker.model.GetMaxLotsResponse
import com.github.trading.core.port.outcome.broker.model.PostOrderResponse
import com.github.trading.core.port.outcome.broker.model.PostOrderResponseStatus
import com.github.trading.core.port.outcome.broker.model.SellLimits
import com.github.trading.domain.model.Candle
import com.github.trading.domain.model.CandleInterval
import com.github.trading.infra.adapter.mapper.commonBrokerMapper
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

@Mapper
abstract class BrokerOutcomeAdapterMapper {

    fun map(source: ru.tinkoff.piapi.contract.v1.PostOrderResponse): PostOrderResponse =
        with(source) {
            PostOrderResponse(
                orderId = orderId,
                status = map(executionReportStatus),
                lotsRequested = lotsRequested.toInt(),
                lotsExecuted = lotsExecuted.toInt(),
                totalPrice = commonBrokerMapper.map(totalOrderAmount),
                executedPrice = commonBrokerMapper.map(executedOrderPrice),
                executedCommission = commonBrokerMapper.map(executedCommission)
            )
        }

    fun map(source: OrderExecutionReportStatus): PostOrderResponseStatus =
        when (source) {
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_UNSPECIFIED -> PostOrderResponseStatus.UNSPECIFIED
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL -> PostOrderResponseStatus.FILL
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_REJECTED -> PostOrderResponseStatus.REJECTED
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_CANCELLED -> PostOrderResponseStatus.CANCELLED
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_NEW -> PostOrderResponseStatus.NEW
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_PARTIALLYFILL -> PostOrderResponseStatus.PARTIALLY_FILL
            OrderExecutionReportStatus.UNRECOGNIZED -> PostOrderResponseStatus.UNRECOGNIZED
        }

    fun map(source: ru.tinkoff.piapi.contract.v1.GetMaxLotsResponse): GetMaxLotsResponse =
        with(source) {
            GetMaxLotsResponse(
                buyLimits = BuyLimits(
                    buyMoneyAmount = commonBrokerMapper.map(buyLimits.buyMoneyAmount),
                    buyMaxLots = buyLimits.buyMaxLots.toInt(),
                    buyMaxMarketLots = buyLimits.buyMaxMarketLots.toInt()
                ),
                buyMarginLimits = BuyLimits(
                    buyMoneyAmount = commonBrokerMapper.map(buyMarginLimits.buyMoneyAmount),
                    buyMaxLots = buyMarginLimits.buyMaxLots.toInt(),
                    buyMaxMarketLots = buyMarginLimits.buyMaxMarketLots.toInt()
                ),
                sellLimits = SellLimits(sellLimits.sellMaxLots.toInt()),
                sellMarginLimits = SellLimits(sellMarginLimits.sellMaxLots.toInt())
            )
        }

    fun mapToSubscriptionInterval(source: CandleInterval): SubscriptionInterval =
        when (source) {
            CandleInterval.ONE_MIN -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
            CandleInterval.FIVE_MIN -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIVE_MINUTES
        }

    fun mapToBrokerCandleInterval(source: CandleInterval): ru.tinkoff.piapi.contract.v1.CandleInterval =
        when (source) {
            CandleInterval.ONE_MIN -> ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_1_MIN
            CandleInterval.FIVE_MIN -> ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_5_MIN
        }

    fun map(historicCandle: HistoricCandle, candleInterval: CandleInterval, instrumentId: String): Candle =
        Candle(
            interval = candleInterval,
            openPrice = commonBrokerMapper.map(historicCandle.open),
            closePrice = commonBrokerMapper.map(historicCandle.close),
            highestPrice = commonBrokerMapper.map(historicCandle.high),
            lowestPrice = commonBrokerMapper.map(historicCandle.low),
            volume = historicCandle.volume,
            endDateTime = commonBrokerMapper.map(historicCandle.time) + candleInterval.duration,
            instrumentId = instrumentId
        )

}

val brokerOutcomeAdapterMapper: BrokerOutcomeAdapterMapper = Mappers.getMapper(BrokerOutcomeAdapterMapper::class.java)