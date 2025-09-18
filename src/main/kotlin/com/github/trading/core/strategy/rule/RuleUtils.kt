package com.github.trading.core.strategy.rule

import com.github.trading.domain.model.Position
import org.ta4j.core.Rule

fun Rule.isSatisfiedByType(index: Int, currentPosition: Position?) =
    when (this) {
        is PositionDependentAbstractRule -> isSatisfied(index, currentPosition)
        else -> isSatisfied(index)
    }