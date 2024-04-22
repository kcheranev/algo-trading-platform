package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.common.DateSupplier
import ru.kcheranev.trading.common.toMskInstant
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesForLongPeriodCommand
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.tinkoff.piapi.core.MarketDataService
import java.time.LocalDate

@Component
class HistoricCandleBrokerOutcomeAdapter(
    tradingProperties: TradingProperties,
    private val marketDataService: MarketDataService,
    private val dateSupplier: DateSupplier
) : HistoricCandleBrokerPort {

    private val startTradingTime = tradingProperties.startTradingTime

    private val endTradingTime = tradingProperties.endTradingTime

    override fun getHistoricCandles(command: GetHistoricCandlesCommand) =
        marketDataService.getCandlesSync(
            command.instrument.id,
            command.from.toMskInstant(),
            command.to.toMskInstant(),
            brokerOutcomeAdapterMapper.mapToBrokerCandleInterval(command.candleInterval)
        ).filter { it.isComplete }
            .map { brokerOutcomeAdapterMapper.map(it, command.candleInterval, command.instrument.id) }
            .sortedBy { it.endTime }

    override fun getHistoricCandlesForLongPeriod(
        command: GetHistoricCandlesForLongPeriodCommand
    ): Map<LocalDate, List<Candle>> {
        val startDay = command.from.toLocalDate()
        val endDay = command.to.toLocalDate()
        val resultMap = mutableMapOf<LocalDate, List<Candle>>()
        var currentDay = startDay
        while (currentDay <= endDay) {
            val startTime =
                if (currentDay == startDay) {
                    command.from.toLocalTime()
                } else {
                    startTradingTime
                }
            val endTime =
                if (currentDay == endDay) {
                    command.to.toLocalTime()
                } else {
                    endTradingTime
                }
            resultMap[currentDay] =
                getHistoricCandles(
                    GetHistoricCandlesCommand(
                        command.instrument,
                        command.candleInterval,
                        currentDay.atTime(startTime),
                        currentDay.atTime(endTime)
                    )
                )
            currentDay = currentDay.plusDays(1)
        }
        return resultMap
    }

    override fun getLastHistoricCandles(command: GetLastHistoricCandlesCommand): List<Candle> {
        val to = dateSupplier.currentDate()
        val from = to - command.candleInterval.duration.multipliedBy(command.quantity.toLong())
        return getHistoricCandles(
            GetHistoricCandlesCommand(command.instrument, command.candleInterval, from, to)
        )
    }

}