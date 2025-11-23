package com.github.trading.domain.model.backtesting

import java.math.BigDecimal

data class StrategyParametersMutation(
    var divisionFactor: BigDecimal,
    var variantsCount: Int
)