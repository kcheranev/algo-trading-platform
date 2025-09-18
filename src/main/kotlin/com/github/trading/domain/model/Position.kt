package com.github.trading.domain.model

import java.math.BigDecimal

data class Position(
    val lotsQuantity: Int,
    val averagePrice: BigDecimal,
    val margin: Boolean
)