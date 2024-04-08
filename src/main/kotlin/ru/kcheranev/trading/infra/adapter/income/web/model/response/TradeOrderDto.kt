package ru.kcheranev.trading.infra.adapter.income.web.model.response

import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class TradeOrderDto(
    var id: UUID,
    var ticker: String,
    var instrumentId: String,
    var date: LocalDateTime,
    var lotsQuantity: Int,
    var totalPrice: BigDecimal,
    var executedCommission: BigDecimal,
    var direction: TradeDirection,
    var strategyConfigurationId: UUID
)