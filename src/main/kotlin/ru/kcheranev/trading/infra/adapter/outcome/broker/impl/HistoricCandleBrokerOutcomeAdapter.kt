package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import ru.kcheranev.trading.common.date.DateSupplier
import ru.kcheranev.trading.common.date.atEndOfDay
import ru.kcheranev.trading.common.date.getOrPut
import ru.kcheranev.trading.common.date.isWeekend
import ru.kcheranev.trading.common.date.toMskInstant
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesForLongPeriodCommand
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.domain.exception.InfrastructureException
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.tinkoff.piapi.core.MarketDataService

private const val HISTORIC_CANDLES_CACHE = "historicCandlesCache"

@Component
class HistoricCandleBrokerOutcomeAdapter(
    private val marketDataService: MarketDataService,
    private val dateSupplier: DateSupplier,
    cacheManager: CacheManager
) : HistoricCandleBrokerPort {

    private val historicCandlesCache =
        cacheManager.getCache(HISTORIC_CANDLES_CACHE)
            ?: throw InfrastructureException("There is no $HISTORIC_CANDLES_CACHE")

    private fun GetHistoricCandlesCommand.digest() = "${instrument.id}_${candleInterval}_${from}_${to}"

    override fun getHistoricCandles(command: GetHistoricCandlesCommand) =
        historicCandlesCache.getOrPut(command.digest()) {
            marketDataService.getCandlesSync(
                command.instrument.id,
                command.from.toMskInstant(),
                command.to.toMskInstant(),
                brokerOutcomeAdapterMapper.mapToBrokerCandleInterval(command.candleInterval)
            ).filter { it.isComplete }
                .map { brokerOutcomeAdapterMapper.map(it, command.candleInterval, command.instrument.id) }
                .sortedBy { it.endDateTime }
        }

    override fun getHistoricCandlesForLongPeriod(
        command: GetHistoricCandlesForLongPeriodCommand
    ): List<Candle> {
        val startDay = command.from
        val endDay = command.to
        var currentDay = startDay
        val candles = mutableListOf<Candle>()
        while (currentDay <= endDay) {
            if (currentDay.isWeekend()) {
                currentDay = currentDay.plusDays(1)
                continue
            }
            candles +=
                getHistoricCandles(
                    GetHistoricCandlesCommand(
                        command.instrument,
                        command.candleInterval,
                        currentDay.atStartOfDay(),
                        currentDay.atEndOfDay()
                    )
                )
            currentDay = currentDay.plusDays(1)
        }
        return candles
    }

    override fun getLastHistoricCandles(command: GetLastHistoricCandlesCommand): List<Candle> {
        val now = dateSupplier.currentDateTime()
        var currentDay = now.toLocalDate()
        var candles =
            getHistoricCandles(
                GetHistoricCandlesCommand(
                    instrument = command.instrument,
                    candleInterval = command.candleInterval,
                    from = currentDay.atStartOfDay(),
                    to = now
                )
            )
        while (candles.size < command.quantity) {
            currentDay = currentDay.minusDays(1)
            if (currentDay.isWeekend()) {
                continue
            }
            candles =
                getHistoricCandles(
                    GetHistoricCandlesCommand(
                        instrument = command.instrument,
                        candleInterval = command.candleInterval,
                        from = currentDay.atStartOfDay(),
                        to = currentDay.atEndOfDay()
                    )
                ).toMutableList() + candles
        }
        return candles
    }

}