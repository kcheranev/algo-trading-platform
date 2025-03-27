package ru.kcheranev.trading.core.strategy.rule

import org.ta4j.core.Rule
import ru.kcheranev.trading.domain.model.Position

fun Rule.isSatisfiedByType(index: Int, currentPosition: Position?) =
    when (this) {
        is PositionDependentAbstractRule -> isSatisfied(index, currentPosition)
        else -> isSatisfied(index)
    }