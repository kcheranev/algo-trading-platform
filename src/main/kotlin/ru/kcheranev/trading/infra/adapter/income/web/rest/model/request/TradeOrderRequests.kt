package ru.kcheranev.trading.infra.adapter.income.web.rest.model.request

import ru.kcheranev.trading.core.port.model.ComparedField
import ru.kcheranev.trading.core.port.model.Page
import ru.kcheranev.trading.core.port.model.sort.Sort
import ru.kcheranev.trading.core.port.model.sort.TradeOrderSort
import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class SearchTradeOrderRequestDto(
    val id: UUID? = null,
    val ticker: String? = null,
    val instrumentId: String? = null,
    val date: ComparedField<LocalDateTime>? = null,
    val lotsQuantity: ComparedField<Int>? = null,
    val totalPrice: ComparedField<BigDecimal>? = null,
    val direction: TradeDirection? = null,
    val tradeSessionId: UUID? = null,
    val page: Page? = null,
    val sort: Sort<TradeOrderSort>? = null
)