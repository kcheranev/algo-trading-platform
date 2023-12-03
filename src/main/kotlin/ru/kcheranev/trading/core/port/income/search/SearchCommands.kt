package ru.kcheranev.trading.core.port.income.search

import ru.kcheranev.trading.core.port.income.search.model.ComparedField
import ru.kcheranev.trading.core.port.income.search.model.Page
import ru.kcheranev.trading.core.port.income.search.model.Sort
import ru.kcheranev.trading.domain.entity.OrderId
import ru.kcheranev.trading.domain.entity.OrderSort
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.StrategyConfigurationSort
import ru.kcheranev.trading.domain.entity.TradeDirection
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionSort
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import java.math.BigDecimal
import java.time.LocalDateTime

sealed class SearchCommand

data class SearchTradeSessionCommand(
    val id: TradeSessionId?,
    val ticker: String?,
    val instrumentId: String?,
    val status: TradeSessionStatus?,
    val startDate: ComparedField<LocalDateTime>?,
    val candleInterval: CandleInterval?,
    val page: Page?,
    val sort: Sort<TradeSessionSort>?
) : SearchCommand()

data class SearchStrategyConfigurationCommand(
    val id: StrategyConfigurationId?,
    val type: StrategyType?,
    val candleInterval: CandleInterval?,
    val page: Page?,
    val sort: Sort<StrategyConfigurationSort>?
) : SearchCommand()

data class SearchOrderCommand(
    val id: OrderId?,
    val ticker: String?,
    val instrumentId: String?,
    val date: ComparedField<LocalDateTime>?,
    val quantity: Int?,
    val price: BigDecimal?,
    val direction: TradeDirection?,
    val tradeSessionId: TradeSessionId?,
    val page: Page?,
    val sort: Sort<OrderSort>?
) : SearchCommand()