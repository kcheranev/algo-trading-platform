package ru.kcheranev.trading.infra.adapter.income.web.model.response

import ru.tinkoff.piapi.contract.v1.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderDto(
    var id: Long,
    var ticker: String,
    var instrumentId: String,
    var date: LocalDateTime,
    var quantity: Int,
    var price: BigDecimal,
    var direction: TradeDirection,
    var tradeSessionId: Long
)