package ru.kcheranev.trading.infra.adapter.income.web.model.request

import ru.kcheranev.trading.core.port.common.model.ComparedField
import ru.kcheranev.trading.core.port.common.model.Page
import ru.kcheranev.trading.core.port.common.model.Sort
import ru.kcheranev.trading.domain.entity.OrderSort
import ru.kcheranev.trading.domain.entity.StrategyConfigurationSort
import ru.kcheranev.trading.domain.entity.TradeDirection
import ru.kcheranev.trading.domain.entity.TradeSessionSort
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import java.math.BigDecimal
import java.time.LocalDateTime

sealed class SearchRequest

data class TradeSessionSearchRequest(
    val id: Long?,
    val ticker: String?,
    val instrumentId: String?,
    val status: TradeSessionStatus?,
    val startDate: ComparedField<LocalDateTime>?,
    val candleInterval: CandleInterval?,
    val page: Page?,
    val sort: Sort<TradeSessionSort>?
) : SearchRequest()

data class StrategyConfigurationSearchRequest(
    val id: Long?,
    val type: StrategyType?,
    val candleInterval: CandleInterval?,
    val page: Page?,
    val sort: Sort<StrategyConfigurationSort>?
) : SearchRequest()

data class TradeOrderSearchRequest(
    val id: Long?,
    val ticker: String?,
    val instrumentId: String?,
    val date: ComparedField<LocalDateTime>?,
    val lotsQuantity: ComparedField<Int>?,
    val price: ComparedField<BigDecimal>?,
    val direction: TradeDirection?,
    val tradeSessionId: Long?,
    val page: Page?,
    val sort: Sort<OrderSort>?
) : SearchRequest()