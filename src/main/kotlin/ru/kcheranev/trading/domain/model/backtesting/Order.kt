package ru.kcheranev.trading.domain.model.backtesting

import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime

data class Order(
    val date: LocalDateTime,
    val direction: TradeDirection,
    val price: BigDecimal
)