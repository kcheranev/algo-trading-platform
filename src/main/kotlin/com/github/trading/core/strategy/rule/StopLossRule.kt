package com.github.trading.core.strategy.rule

import com.github.trading.domain.model.Position
import org.ta4j.core.TradingRecord
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.Num
import java.math.BigDecimal

class StopLossRule(
    private val closePrice: ClosePriceIndicator,
    lossPercentage: BigDecimal
) : PositionDependentAbstractRule() {

    private val hundred = closePrice.numOf(100)

    private val lossPercentage = closePrice.numOf(lossPercentage)

    override fun isSatisfied(index: Int, currentPosition: Position?): Boolean {
        var satisfied = false
        if (currentPosition != null) {
            val entryPrice = currentPosition.averagePrice
            val currentPrice = closePrice.getValue(index)
            satisfied =
                if (!currentPosition.margin) {
                    isBuyStopSatisfied(DecimalNum.valueOf(entryPrice), currentPrice)
                } else {
                    isSellStopSatisfied(DecimalNum.valueOf(entryPrice), currentPrice)
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
                        isBuyStopSatisfied(entryPrice, currentPrice)
                    } else {
                        isSellStopSatisfied(entryPrice, currentPrice)
                    }
            }
        }
        traceIsSatisfied(index, satisfied)
        return satisfied
    }

    private fun isBuyStopSatisfied(entryPrice: Num, currentPrice: Num): Boolean {
        val lossRatioThreshold = hundred.minus(lossPercentage).dividedBy(hundred)
        val threshold = entryPrice.multipliedBy(lossRatioThreshold)
        return currentPrice.isLessThanOrEqual(threshold)
    }

    private fun isSellStopSatisfied(entryPrice: Num, currentPrice: Num): Boolean {
        val lossRatioThreshold = hundred.plus(lossPercentage).dividedBy(hundred)
        val threshold = entryPrice.multipliedBy(lossRatioThreshold)
        return currentPrice.isGreaterThanOrEqual(threshold)
    }

}