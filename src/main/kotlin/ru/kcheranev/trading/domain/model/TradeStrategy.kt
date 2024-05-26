package ru.kcheranev.trading.domain.model

import org.ta4j.core.AnalysisCriterion
import org.ta4j.core.Bar
import org.ta4j.core.BarSeries
import org.ta4j.core.Strategy
import org.ta4j.core.Trade.TradeType
import org.ta4j.core.analysis.cost.LinearTransactionCostModel
import org.ta4j.core.analysis.cost.ZeroCostModel
import org.ta4j.core.backtest.BarSeriesManager
import org.ta4j.core.criteria.EnterAndHoldReturnCriterion
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
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.Num
import ru.kcheranev.trading.domain.model.backtesting.DailyStrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.Order
import ru.kcheranev.trading.domain.model.backtesting.Trade
import java.math.BigDecimal

class TradeStrategy(
    val series: BarSeries,
    val margin: Boolean,
    strategy: Strategy
) : Strategy by strategy {

    fun addBar(bar: Bar) {
        series.addBar(bar)
    }

    fun shouldEnter() = shouldEnter(series.endIndex)

    fun shouldExit() = shouldExit(series.endIndex)

    fun analyze(commission: BigDecimal): DailyStrategyAnalyzeResult {
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
        return DailyStrategyAnalyzeResult(
            averageLoss = AverageLossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            averageProfit = AverageProfitCriterion().calculate(series, tradingRecord).toBigDecimal(),
            enterAndHoldReturn = EnterAndHoldReturnCriterion()
                .calculate(series, tradingRecord).toBigDecimal(),
            netLoss = LossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            grossLoss = LossCriterion(true).calculate(series, tradingRecord).toBigDecimal(),
            maximumDrawdown = MaximumDrawdownCriterion().calculate(series, tradingRecord).toBigDecimal(),
            barsCount = NumberOfBarsCriterion().calculate(series, tradingRecord).toInt(),
            consecutiveProfitPositionsCount = NumberOfConsecutivePositionsCriterion(AnalysisCriterion.PositionFilter.PROFIT)
                .calculate(series, tradingRecord).toInt(),
            consecutiveLosingPositionsCount = NumberOfConsecutivePositionsCriterion(AnalysisCriterion.PositionFilter.LOSS)
                .calculate(series, tradingRecord).toInt(),
            losingPositionsCount = NumberOfLosingPositionsCriterion()
                .calculate(series, tradingRecord).toInt(),
            positionsCount = NumberOfPositionsCriterion()
                .calculate(series, tradingRecord).toInt(),
            profitPositionsCount = NumberOfWinningPositionsCriterion()
                .calculate(series, tradingRecord).toInt(),
            netProfit = ProfitCriterion().calculate(series, tradingRecord).toBigDecimal(),
            grossProfit = ProfitCriterion(true).calculate(series, tradingRecord).toBigDecimal(),
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

    private fun Num.toBigDecimal() = (this as DecimalNum).delegate

    private fun Num.toInt() = (this as DecimalNum).delegate.toInt()

}