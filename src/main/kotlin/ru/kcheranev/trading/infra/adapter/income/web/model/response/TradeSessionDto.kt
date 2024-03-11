package ru.kcheranev.trading.infra.adapter.income.web.model.response

import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import java.time.LocalDateTime

data class TradeSessionDto(
    var id: Long,
    var ticker: String,
    var instrumentId: String,
    var status: TradeSessionStatus,
    var startDate: LocalDateTime,
    var candleInterval: CandleInterval,
    var lotsQuantity: Int,
    var strategyConfigurationId: Long
)