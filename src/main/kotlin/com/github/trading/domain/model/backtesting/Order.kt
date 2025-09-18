package com.github.trading.domain.model.backtesting

import com.github.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime

data class Order(
    val date: LocalDateTime,
    val direction: TradeDirection,
    val netPrice: BigDecimal,
    val grossPrice: BigDecimal
)