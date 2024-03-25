package ru.kcheranev.trading.infra.adapter.income.web.model.response

import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import java.time.LocalDateTime
import java.util.UUID

data class TradeSessionDto(
    var id: UUID,
    var ticker: String,
    var instrumentId: String,
    var status: TradeSessionStatus,
    var startDate: LocalDateTime,
    var candleInterval: CandleInterval,
    var lotsQuantity: Int,
    var strategyConfigurationId: UUID
)