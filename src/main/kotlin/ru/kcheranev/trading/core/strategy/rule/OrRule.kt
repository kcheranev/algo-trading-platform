package ru.kcheranev.trading.core.strategy.rule

import org.ta4j.core.Rule
import org.ta4j.core.TradingRecord
import ru.kcheranev.trading.domain.model.Position

class OrRule(
    private val rule1: Rule,
    private val rule2: Rule
) : PositionDependentAbstractRule() {

    override fun isSatisfied(index: Int, currentPosition: Position?): Boolean {
        val satisfied =
            rule1.isSatisfiedByType(index, currentPosition) || rule2.isSatisfiedByType(index, currentPosition)
        traceIsSatisfied(index, satisfied)
        return satisfied
    }

    override fun isSatisfied(index: Int, tradingRecord: TradingRecord?): Boolean {
        val satisfied = rule1.isSatisfied(index, tradingRecord) || rule2.isSatisfied(index, tradingRecord)
        traceIsSatisfied(index, satisfied)
        return satisfied
    }

}