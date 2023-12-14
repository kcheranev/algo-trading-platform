package ru.kcheranev.trading.infra.adapter.outcome.broker

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse
import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderStatus
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.mapper.commonBrokerMapper
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
                lotsRequested = lotsRequested,
                lotsExecuted = lotsExecuted,
                totalPrice = commonBrokerMapper.map(totalOrderAmount),
                executedCommission = commonBrokerMapper.map(executedCommission)
            )
        }

    fun map(source: OrderExecutionReportStatus): PostOrderStatus =
        when (source) {
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_UNSPECIFIED -> PostOrderStatus.UNSPECIFIED
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL -> PostOrderStatus.FILL
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_REJECTED -> PostOrderStatus.REJECTED
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_CANCELLED -> PostOrderStatus.CANCELLED
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_NEW -> PostOrderStatus.NEW
            OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_PARTIALLYFILL -> PostOrderStatus.PARTIALLY_FILL
            OrderExecutionReportStatus.UNRECOGNIZED -> PostOrderStatus.UNRECOGNIZED
        }

    fun mapToSubscriptionInterval(source: CandleInterval): SubscriptionInterval =
        when (source) {
            CandleInterval.UNDEFINED -> throw UnexpectedCandleIntervalException(source)
            CandleInterval.ONE_MIN -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
            CandleInterval.FIVE_MIN -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIVE_MINUTES
        }

    fun mapToBrokerCandleInterval(source: CandleInterval): ru.tinkoff.piapi.contract.v1.CandleInterval =
        when (source) {
            CandleInterval.UNDEFINED -> throw UnexpectedCandleIntervalException(source)
            CandleInterval.ONE_MIN -> ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_1_MIN
            CandleInterval.FIVE_MIN -> ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_5_MIN
        }

    fun map(historicCandle: HistoricCandle, candleInterval: CandleInterval, instrumentId: String): Candle {
        return Candle(
            interval = candleInterval,
            openPrice = commonBrokerMapper.map(historicCandle.open),
            closePrice = commonBrokerMapper.map(historicCandle.close),
            highestPrice = commonBrokerMapper.map(historicCandle.high),
            lowestPrice = commonBrokerMapper.map(historicCandle.low),
            volume = historicCandle.volume,
            endTime = commonBrokerMapper.map(historicCandle.time).plus(candleInterval.duration),
            instrumentId = instrumentId
        )
    }

}

val brokerOutcomeAdapterMapper: BrokerOutcomeAdapterMapper = Mappers.getMapper(BrokerOutcomeAdapterMapper::class.java)