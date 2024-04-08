package ru.kcheranev.trading.infra.adapter.income.web.model.request

import ru.kcheranev.trading.core.port.common.model.ComparedField
import ru.kcheranev.trading.core.port.common.model.Page
import ru.kcheranev.trading.core.port.common.model.sort.Sort
import ru.kcheranev.trading.core.port.common.model.sort.StrategyConfigurationSort
import ru.kcheranev.trading.core.port.common.model.sort.TradeOrderSort
import ru.kcheranev.trading.core.port.common.model.sort.TradeSessionSort
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class TradeSessionSearchRequestDto(
    val id: UUID? = null,
    val ticker: String? = null,
    val instrumentId: String? = null,
    val status: TradeSessionStatus? = null,
    val candleInterval: CandleInterval? = null,
    val sort: Sort<TradeSessionSort>? = null
)

data class StrategyConfigurationSearchRequestDto(
    val id: UUID? = null,
    val type: String? = null,
    val candleInterval: CandleInterval? = null,
    val page: Page? = null,
    val sort: Sort<StrategyConfigurationSort>? = null
)

data class TradeOrderSearchRequestDto(
    val id: UUID? = null,
    val ticker: String? = null,
    val instrumentId: String? = null,
    val date: ComparedField<LocalDateTime>? = null,
    val lotsQuantity: ComparedField<Int>? = null,
    val totalPrice: ComparedField<BigDecimal>? = null,
    val direction: TradeDirection? = null,
    val strategyConfigurationId: UUID? = null,
    val page: Page? = null,
    val sort: Sort<TradeOrderSort>? = null
)