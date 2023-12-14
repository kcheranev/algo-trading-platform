package ru.kcheranev.trading.infra.adapter.income.web.model.request

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.StrategyType

sealed class TradingRequest

data class CreateStrategyConfigurationRequest(
    val type: StrategyType,
    val initCandleAmount: Int,
    val candleInterval: CandleInterval,
    val params: Map<String, Any>
)

data class StartTradeSessionRequest(
    val strategyConfigurationId: Long,
    val lotsQuantity: Int,
    val instrument: Instrument,
    val strategyType: StrategyType
) : TradingRequest()