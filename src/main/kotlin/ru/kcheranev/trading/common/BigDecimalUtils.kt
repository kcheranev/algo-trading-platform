package ru.kcheranev.trading.common

import java.math.BigDecimal
import java.math.RoundingMode

fun max(value1: BigDecimal, value2: BigDecimal) = if (value1 > value2) value1 else value2

fun min(value1: BigDecimal, value2: BigDecimal) = if (value1 > value2) value2 else value1

fun BigDecimal.format(maxFractionDigits: Int = 4): String =
    setScale(maxFractionDigits, RoundingMode.HALF_UP).toPlainString()