package com.github.trading.core.strategy.rule

import com.github.trading.domain.model.Position
import org.ta4j.core.Rule
import org.ta4j.core.rules.AbstractRule

abstract class PositionDependentAbstractRule : AbstractRule() {

    abstract fun isSatisfied(index: Int, currentPosition: Position?): Boolean

    override fun and(rule: Rule) = AndRule(this, rule)

    override fun or(rule: Rule) = OrRule(this, rule)

}