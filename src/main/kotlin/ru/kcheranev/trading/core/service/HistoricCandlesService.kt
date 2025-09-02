package ru.kcheranev.trading.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.port.income.historiccandles.StoreHistoricCandlesCommand
import ru.kcheranev.trading.core.port.income.historiccandles.StoreHistoricCandlesUseCase
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesForLongPeriodCommand
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.HistoricCandleBrokerOutcomeAdapter
import java.io.File

@Service
class HistoricCandlesService(
    private val historicCandleBrokerOutcomeAdapter: HistoricCandleBrokerOutcomeAdapter,
    private val objectMapper: ObjectMapper
) : StoreHistoricCandlesUseCase {

    override fun storeHistoricCandles(command: StoreHistoricCandlesCommand) {
        val (instrument, from, to, candleInterval) = command
        val getHistoricCandlesForLongPeriodCommand =
            GetHistoricCandlesForLongPeriodCommand(
                instrument = Instrument(id = instrument.id, ticker = instrument.ticker),
                from = from,
                to = to,
                candleInterval = candleInterval
            )
        val candles = historicCandleBrokerOutcomeAdapter.getHistoricCandlesForLongPeriod(getHistoricCandlesForLongPeriodCommand)
        val fileName = "${instrument.ticker}_from-${from}_to-${to}_$candleInterval"
        File("$fileName.json").writeText(objectMapper.writeValueAsString(candles))
    }

}