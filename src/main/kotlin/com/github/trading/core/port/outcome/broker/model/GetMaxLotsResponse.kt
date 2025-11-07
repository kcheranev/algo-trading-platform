package com.github.trading.core.port.outcome.broker.model

import java.math.BigDecimal

data class GetMaxLotsResponse(
    val buyLimits: BuyLimits,
    val buyMarginLimits: BuyLimits,
    val sellLimits: SellLimits,
    val sellMarginLimits: SellLimits
)

data class BuyLimits(
    val buyMoneyAmount: BigDecimal,
    val buyMaxLots: Int,
    val buyMaxMarketLots: Int
)

data class SellLimits(
    val sellMaxLots: Int
)