package ru.kcheranev.trading.domain.model

import java.math.BigDecimal

data class Portfolio(
    val currencyAmount: BigDecimal,
    val sharesAmount: BigDecimal,
    val totalPortfolioAmount: BigDecimal
)
