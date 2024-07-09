package ru.kcheranev.trading.core.port.outcome.persistence.tradeorder

import ru.kcheranev.trading.core.port.model.ComparedField
import ru.kcheranev.trading.core.port.model.Page
import ru.kcheranev.trading.core.port.model.sort.Sort
import ru.kcheranev.trading.core.port.model.sort.TradeOrderSort
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeOrderId
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime

data class SaveOrderCommand(
    val tradeOrder: TradeOrder
)

data class GetOrderCommand(
    val tradeOrderId: TradeOrderId
)

data class SearchTradeOrderCommand(
    val id: TradeOrderId?,
    val ticker: String?,
    val instrumentId: String?,
    val date: ComparedField<LocalDateTime>?,
    val lotsQuantity: ComparedField<Int>?,
    val totalPrice: ComparedField<BigDecimal>?,
    val direction: TradeDirection?,
    val tradeSessionId: TradeSessionId?,
    val page: Page?,
    val sort: Sort<TradeOrderSort>?
)