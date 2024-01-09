package ru.kcheranev.trading.infra.adapter.income.web.model.request

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument

sealed class TradingRequest

data class CreateStrategyConfigurationRequest(
    val type: String,
    val initCandleAmount: Int,
    val candleInterval: CandleInterval,
    val params: Map<String, Any>
)

data class StartTradeSessionRequest(
    val strategyConfigurationId: Long,
    val lotsQuantity: Int,
    val instrument: Instrument
) : TradingRequest()