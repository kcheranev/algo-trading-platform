package ru.kcheranev.trading.core.port.income.search

import ru.kcheranev.trading.core.port.common.model.ComparedField
import ru.kcheranev.trading.core.port.common.model.Page
import ru.kcheranev.trading.core.port.common.model.sort.Sort
import ru.kcheranev.trading.core.port.common.model.sort.StrategyConfigurationSort
import ru.kcheranev.trading.core.port.common.model.sort.TradeOrderSort
import ru.kcheranev.trading.core.port.common.model.sort.TradeSessionSort
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeDirection
import ru.kcheranev.trading.domain.entity.TradeOrderId
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import java.math.BigDecimal
import java.time.LocalDateTime

sealed class SearchIncomeCommand

data class TradeSessionSearchCommand(
    val id: TradeSessionId?,
    val ticker: String?,
    val instrumentId: String?,
    val status: TradeSessionStatus?,
    val candleInterval: CandleInterval?,
    val sort: Sort<TradeSessionSort>?
) : SearchIncomeCommand()

data class StrategyConfigurationSearchCommand(
    val id: StrategyConfigurationId?,
    val type: String?,
    val candleInterval: CandleInterval?,
    val page: Page?,
    val sort: Sort<StrategyConfigurationSort>?
) : SearchIncomeCommand()

data class TradeOrderSearchCommand(
    val id: TradeOrderId?,
    val ticker: String?,
    val instrumentId: String?,
    val date: ComparedField<LocalDateTime>?,
    val lotsQuantity: ComparedField<Int>?,
    val totalPrice: ComparedField<BigDecimal>?,
    val direction: TradeDirection?,
    val strategyConfigurationId: StrategyConfigurationId?,
    val page: Page?,
    val sort: Sort<TradeOrderSort>?
) : SearchIncomeCommand()