package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.common.MskDateUtil
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.kcheranev.trading.infra.config.BrokerApi
import java.time.LocalDateTime

@Component
class HistoricCandleBrokerOutcomeAdapter(brokerApi: BrokerApi) : HistoricCandleBrokerPort {

    private val marketDataService = brokerApi.marketDataService

    override fun getHistoricCandles(command: GetHistoricCandlesCommand): List<Candle> =
        marketDataService.getCandles(
            command.instrument.id,
            MskDateUtil.toInstant(command.from),
            MskDateUtil.toInstant(command.to),
            brokerOutcomeAdapterMapper.mapToBrokerCandleInterval(command.candleInterval)
        ).get()
            .filter { it.isComplete }
            .map { brokerOutcomeAdapterMapper.map(it, command.candleInterval, command.instrument.id) }

    override fun getLastHistoricCandles(command: GetLastHistoricCandlesCommand): List<Candle> {
        val to = LocalDateTime.now()
        val from = to.minus(command.candleInterval.duration.multipliedBy(command.quantity.toLong()))
        return getHistoricCandles(
            GetHistoricCandlesCommand(
                command.instrument,
                command.candleInterval,
                from,
                to
            )
        )
    }

}