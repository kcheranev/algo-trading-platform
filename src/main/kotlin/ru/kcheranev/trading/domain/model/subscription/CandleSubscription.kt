package ru.kcheranev.trading.domain.model.subscription

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument

private const val CANDLES_STREAM_ID_FORMAT = "candles_%s_%s"

data class CandleSubscription(
    val instrument: Instrument,
    val candleInterval: CandleInterval
) {

    val id
        get() = CANDLES_STREAM_ID_FORMAT.format(instrument.ticker, candleInterval)

}