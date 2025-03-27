package ru.kcheranev.trading.core.strategy.rule

import org.ta4j.core.Rule
import org.ta4j.core.rules.AbstractRule
import ru.kcheranev.trading.domain.model.Position

abstract class PositionDependentAbstractRule : AbstractRule() {

    abstract fun isSatisfied(index: Int, currentPosition: Position?): Boolean

    override fun and(rule: Rule) = AndRule(this, rule)

    override fun or(rule: Rule) = OrRule(this, rule)

}