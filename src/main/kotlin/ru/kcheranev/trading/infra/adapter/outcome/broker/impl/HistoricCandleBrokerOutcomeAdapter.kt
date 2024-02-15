package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.common.DateSupplier
import ru.kcheranev.trading.common.MskDateUtil
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.kcheranev.trading.infra.config.BrokerApi

@Component
class HistoricCandleBrokerOutcomeAdapter(
    brokerApi: BrokerApi,
    val dateSupplier: DateSupplier
) : HistoricCandleBrokerPort {

    private val marketDataService = brokerApi.marketDataService

    override fun getHistoricCandles(command: GetHistoricCandlesCommand) =
        marketDataService.getCandlesSync(
            command.instrument.id,
            MskDateUtil.toInstant(command.from),
            MskDateUtil.toInstant(command.to),
            brokerOutcomeAdapterMapper.mapToBrokerCandleInterval(command.candleInterval)
        ).filter { it.isComplete }
            .map { brokerOutcomeAdapterMapper.map(it, command.candleInterval, command.instrument.id) }
            .sortedBy { it.endTime }

    override fun getLastHistoricCandles(command: GetLastHistoricCandlesCommand): List<Candle> {
        val to = dateSupplier.currentDate()
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