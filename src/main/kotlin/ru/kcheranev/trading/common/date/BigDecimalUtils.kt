package ru.kcheranev.trading.common.date

import java.math.BigDecimal

fun max(value1: BigDecimal, value2: BigDecimal) = if (value1 > value2) value1 else value2

fun min(value1: BigDecimal, value2: BigDecimal) = if (value1 > value2) value2 else value1