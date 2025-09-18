package com.github.trading.infra.adapter.outcome.broker.impl

import com.github.trading.common.date.DateSupplier
import com.github.trading.common.date.atEndOfDay
import com.github.trading.common.date.isWeekend
import com.github.trading.common.date.toMskInstant
import com.github.trading.common.getOrPut
import com.github.trading.core.port.outcome.broker.GetHistoricCandlesCommand
import com.github.trading.core.port.outcome.broker.GetHistoricCandlesForLongPeriodCommand
import com.github.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import com.github.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import com.github.trading.domain.exception.InfrastructureException
import com.github.trading.domain.model.Candle
import com.github.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import ru.tinkoff.piapi.core.MarketDataService

private const val HISTORIC_CANDLES_CACHE = "historicCandles"

@Component
class HistoricCandleBrokerOutcomeAdapter(
    private val brokerMarketDataService: MarketDataService,
    cacheManager: CacheManager
) : HistoricCandleBrokerPort {

    private val historicCandlesCache =
        cacheManager.getCache(HISTORIC_CANDLES_CACHE)
            ?: throw InfrastructureException("There is no $HISTORIC_CANDLES_CACHE")

    private fun GetHistoricCandlesCommand.digest() = "${instrument.id}_${candleInterval}_${from}_${to}"

    override fun getHistoricCandles(command: GetHistoricCandlesCommand) =
        historicCandlesCache.getOrPut(command.digest()) {
            brokerMarketDataService.getCandlesSync(
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
        val now = DateSupplier.currentDateTime()
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