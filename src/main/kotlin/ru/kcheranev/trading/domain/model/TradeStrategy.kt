package ru.kcheranev.trading.domain.model

import org.ta4j.core.AnalysisCriterion
import org.ta4j.core.Bar
import org.ta4j.core.BarSeries
import org.ta4j.core.Strategy
import org.ta4j.core.Trade.TradeType
import org.ta4j.core.analysis.cost.LinearTransactionCostModel
import org.ta4j.core.analysis.cost.ZeroCostModel
import org.ta4j.core.backtest.BarSeriesManager
import org.ta4j.core.criteria.EnterAndHoldCriterion
import org.ta4j.core.criteria.MaximumDrawdownCriterion
import org.ta4j.core.criteria.NumberOfBarsCriterion
import org.ta4j.core.criteria.NumberOfConsecutivePositionsCriterion
import org.ta4j.core.criteria.NumberOfLosingPositionsCriterion
import org.ta4j.core.criteria.NumberOfPositionsCriterion
import org.ta4j.core.criteria.NumberOfWinningPositionsCriterion
import org.ta4j.core.criteria.pnl.AverageLossCriterion
import org.ta4j.core.criteria.pnl.AverageProfitCriterion
import org.ta4j.core.criteria.pnl.LossCriterion
import org.ta4j.core.criteria.pnl.ProfitCriterion
import org.ta4j.core.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion
import org.ta4j.core.criteria.pnl.ProfitLossRatioCriterion
import org.ta4j.core.criteria.pnl.ReturnCriterion
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.Num
import ru.kcheranev.trading.common.date.isWeekend
import ru.kcheranev.trading.common.date.max
import ru.kcheranev.trading.common.date.min
import ru.kcheranev.trading.core.config.TradingProperties.Companion.tradingProperties
import ru.kcheranev.trading.domain.model.backtesting.Order
import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.Trade
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class TradeStrategy(
    private val series: BarSeries,
    val margin: Boolean,
    strategy: Strategy
) : Strategy by strategy {

    fun addBar(bar: Bar) {
        series.addBar(bar)
    }

    fun shouldEnter() = shouldEnter(series.endIndex)

    fun shouldExit() = shouldExit(series.endIndex)

    fun lastCandleDate(): LocalDateTime? =
        if (series.isEmpty) {
            null
        } else {
            series.lastBar
                .endTime
                .toLocalDateTime()
        }

    fun isFreshCandleSeries(targetDate: LocalDateTime, candleInterval: CandleInterval): Boolean {
        val lastCandleDate = lastCandleDate() ?: return false
        if (Duration.between(lastCandleDate, targetDate)
                .dividedBy(candleInterval.duration) <= tradingProperties.availableDelayedCandlesCount
        ) {
            return true
        }
        var currentDay = lastCandleDate.toLocalDate()
        var skippedCandlesCount = 0
        while (currentDay <= targetDate.toLocalDate()) {
            if (currentDay.isWeekend()) {
                currentDay = currentDay.plusDays(1)
                continue
            }
            val startTime =
                if (currentDay == lastCandleDate.toLocalDate()) {
                    lastCandleDate.toLocalTime()
                } else {
                    LocalTime.MIN
                }
            val endTime =
                if (currentDay == targetDate.toLocalDate()) {
                    targetDate.toLocalTime()
                } else {
                    LocalTime.MAX
                }
            tradingProperties.tradingSchedule
                .filter { it.afterOrContains(startTime) }
                .filter { it.beforeOrContains(endTime) }
                .map { Duration.between(max(it.from, startTime), min(it.to, endTime)) }
                .forEach { skippedCandlesCount += it.dividedBy(candleInterval.duration).toInt() }
            currentDay = currentDay.plusDays(1)
        }
        return skippedCandlesCount <= tradingProperties.availableDelayedCandlesCount
    }

    fun analyze(commission: BigDecimal): StrategyAnalyzeResult {
        val tradeType = if (margin) TradeType.SELL else TradeType.BUY
        val tradingRecord =
            BarSeriesManager(series, LinearTransactionCostModel(commission.toDouble()), ZeroCostModel())
                .run(this, tradeType)
        val trades =
            tradingRecord.positions
                .map { position ->
                    Trade(mapPositionTrade(position.entry), mapPositionTrade(position.exit))
                }
                .toMutableList()
        if (tradingRecord.currentPosition.entry != null && tradingRecord.currentPosition.exit == null) {
            trades.add(Trade(mapPositionTrade(tradingRecord.currentPosition.entry), null))
        }
        return StrategyAnalyzeResult(
            netProfit = ProfitCriterion().calculate(series, tradingRecord).toBigDecimal(),
            grossProfit = ProfitCriterion(true).calculate(series, tradingRecord).toBigDecimal(),
            netLoss = LossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            grossLoss = LossCriterion(true).calculate(series, tradingRecord).toBigDecimal(),
            profitPositionsCount = NumberOfWinningPositionsCriterion().calculate(series, tradingRecord).toInt(),
            losingPositionsCount = NumberOfLosingPositionsCriterion().calculate(series, tradingRecord).toInt(),
            positionsCount = NumberOfPositionsCriterion().calculate(series, tradingRecord).toInt(),
            consecutiveProfitPositionsCount = NumberOfConsecutivePositionsCriterion(AnalysisCriterion.PositionFilter.PROFIT)
                .calculate(series, tradingRecord).toInt(),
            consecutiveLosingPositionsCount = NumberOfConsecutivePositionsCriterion(AnalysisCriterion.PositionFilter.LOSS)
                .calculate(series, tradingRecord).toInt(),
            averageLoss = AverageLossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            averageProfit = AverageProfitCriterion().calculate(series, tradingRecord).toBigDecimal(),
            enterAndHoldReturn = EnterAndHoldCriterion(ReturnCriterion()).calculate(series, tradingRecord)
                .toBigDecimal(),
            maximumDrawdown = MaximumDrawdownCriterion().calculate(series, tradingRecord).toBigDecimal(),
            barsCount = NumberOfBarsCriterion().calculate(series, tradingRecord).toInt(),
            profitLoss = ProfitLossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            profitLossPercentage = ProfitLossPercentageCriterion().calculate(series, tradingRecord).toBigDecimal(),
            profitLossRatio = ProfitLossRatioCriterion().calculate(series, tradingRecord).toBigDecimal(),
            trades = trades
        )
    }

    private fun mapPositionTrade(trade: org.ta4j.core.Trade) =
        Order(
            date = series.getBar(trade.index).beginTime.toLocalDateTime(),
            direction = TradeDirection.valueOf(trade.type.name),
            netPrice = trade.netPrice.toBigDecimal(),
            grossPrice = trade.pricePerAsset.toBigDecimal()
        )

}

private fun Num.toBigDecimal() = (this as DecimalNum).delegate

private fun Num.toInt() = (this as DecimalNum).delegate.toInt()