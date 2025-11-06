package com.github.trading.core.strategy.rule

import com.github.trading.domain.model.Position
import org.ta4j.core.TradingRecord
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.Num
import java.math.BigDecimal

class StopGainRule(
    private val closePrice: ClosePriceIndicator,
    gainPercentage: BigDecimal
) : PositionDependentAbstractRule() {

    private val hundred = closePrice.barSeries.numFactory().hundred()

    private val gainPercentage = closePrice.barSeries.numFactory().numOf(gainPercentage)

    override fun isSatisfied(index: Int, currentPosition: Position?): Boolean {
        var satisfied = false
        if (currentPosition != null) {
            val entryPrice = currentPosition.averagePrice
            val currentPrice = closePrice.getValue(index)
            satisfied =
                if (!currentPosition.margin) {
                    isBuyGainSatisfied(DecimalNum.valueOf(entryPrice), currentPrice)
                } else {
                    isSellGainSatisfied(DecimalNum.valueOf(entryPrice), currentPrice)
                }
        }
        traceIsSatisfied(index, satisfied)
        return satisfied
    }

    override fun isSatisfied(index: Int, tradingRecord: TradingRecord?): Boolean {
        var satisfied = false
        if (tradingRecord != null) {
            val currentPosition = tradingRecord.currentPosition
            if (currentPosition.isOpened) {
                val entryPrice = currentPosition.entry.netPrice
                val currentPrice = closePrice.getValue(index)
                satisfied =
                    if (currentPosition.entry.isBuy) {
                        isBuyGainSatisfied(entryPrice, currentPrice)
                    } else {
                        isSellGainSatisfied(entryPrice, currentPrice)
                    }
            }
        }
        traceIsSatisfied(index, satisfied)
        return satisfied
    }

    private fun isBuyGainSatisfied(entryPrice: Num, currentPrice: Num): Boolean {
        val lossRatioThreshold = hundred.plus(gainPercentage).dividedBy(hundred)
        val threshold = entryPrice.multipliedBy(lossRatioThreshold)
        return currentPrice.isGreaterThanOrEqual(threshold)
    }

    private fun isSellGainSatisfied(entryPrice: Num, currentPrice: Num): Boolean {
        val lossRatioThreshold = hundred.minus(gainPercentage).dividedBy(hundred)
        val threshold = entryPrice.multipliedBy(lossRatioThreshold)
        return currentPrice.isLessThanOrEqual(threshold)
    }

}