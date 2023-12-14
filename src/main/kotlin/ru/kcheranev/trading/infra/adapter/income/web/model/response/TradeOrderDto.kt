package ru.kcheranev.trading.infra.adapter.income.web.model.response

import ru.kcheranev.trading.domain.entity.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime

data class TradeOrderDto(
    var id: Long,
    var ticker: String,
    var instrumentId: String,
    var date: LocalDateTime,
    var lotsQuantity: Int,
    var price: BigDecimal,
    var direction: TradeDirection,
    var tradeSessionId: Long
)