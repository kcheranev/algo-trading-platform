package ru.kcheranev.trading.domain.model

import org.ta4j.core.AnalysisCriterion
import org.ta4j.core.Bar
import org.ta4j.core.BarSeries
import org.ta4j.core.BarSeriesManager
import org.ta4j.core.Strategy
import org.ta4j.core.criteria.EnterAndHoldReturnCriterion
import org.ta4j.core.criteria.MaximumDrawdownCriterion
import org.ta4j.core.criteria.NumberOfBarsCriterion
import org.ta4j.core.criteria.NumberOfConsecutivePositionsCriterion
import org.ta4j.core.criteria.NumberOfLosingPositionsCriterion
import org.ta4j.core.criteria.NumberOfPositionsCriterion
import org.ta4j.core.criteria.NumberOfWinningPositionsCriterion
import org.ta4j.core.criteria.pnl.AverageLossCriterion
import org.ta4j.core.criteria.pnl.AverageProfitCriterion
import org.ta4j.core.criteria.pnl.GrossProfitCriterion
import org.ta4j.core.criteria.pnl.NetLossCriterion
import org.ta4j.core.criteria.pnl.NetProfitCriterion
import org.ta4j.core.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion
import org.ta4j.core.criteria.pnl.ProfitLossRatioCriterion
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.Num
import ru.kcheranev.trading.domain.model.backtesting.Order
import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.Trade

class TradeStrategy(
    val series: BarSeries,
    strategy: Strategy
) : Strategy by strategy {

    fun addBar(bar: Bar) {
        series.addBar(bar)
    }

    fun shouldEnter() = shouldEnter(series.endIndex)

    fun shouldExit() = shouldExit(series.endIndex)

    fun analyze(): StrategyAnalyzeResult {
        val tradingRecord = BarSeriesManager(series).run(this)
        val trades =
            tradingRecord.positions
                .map { position ->
                    Trade(
                        entry = mapPositionTrade(position.entry),
                        exit = mapPositionTrade(position.exit)
                    )
                }
        return StrategyAnalyzeResult(
            averageLoss = AverageLossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            averageProfit = AverageProfitCriterion().calculate(series, tradingRecord).toBigDecimal(),
            enterAndHoldReturn = EnterAndHoldReturnCriterion()
                .calculate(series, tradingRecord).toBigDecimal(),
            netLoss = NetLossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            grossLoss = NetLossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            maximumDrawdown = MaximumDrawdownCriterion().calculate(series, tradingRecord).toBigDecimal(),
            numberOfBars = NumberOfBarsCriterion().calculate(series, tradingRecord).toInt(),
            numberOfConsecutiveProfitPositions = NumberOfConsecutivePositionsCriterion(AnalysisCriterion.PositionFilter.PROFIT)
                .calculate(series, tradingRecord).toInt(),
            numberOfConsecutiveLosingPositions = NumberOfConsecutivePositionsCriterion(AnalysisCriterion.PositionFilter.LOSS)
                .calculate(series, tradingRecord).toInt(),
            numberOfLosingPositions = NumberOfLosingPositionsCriterion()
                .calculate(series, tradingRecord).toInt(),
            numberOfPositions = NumberOfPositionsCriterion()
                .calculate(series, tradingRecord).toInt(),
            numberOfProfitPositions = NumberOfWinningPositionsCriterion()
                .calculate(series, tradingRecord).toInt(),
            netProfit = NetProfitCriterion().calculate(series, tradingRecord).toBigDecimal(),
            grossProfit = GrossProfitCriterion().calculate(series, tradingRecord).toBigDecimal(),
            profitLoss = ProfitLossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            profitLossPercentage = ProfitLossPercentageCriterion().calculate(series, tradingRecord).toBigDecimal(),
            profitLossRatio = ProfitLossRatioCriterion().calculate(series, tradingRecord).toBigDecimal(),
            trades = trades
        )
    }

    private fun mapPositionTrade(trade: org.ta4j.core.Trade) =
        Order(
            date = series.getBar(trade.index - 1).endTime.toLocalDateTime(),
            direction = TradeDirection.valueOf(trade.type.name),
            price = trade.netPrice.toBigDecimal()
        )

    private fun Num.toBigDecimal() = (this as DecimalNum).delegate

    private fun Num.toInt() = (this as DecimalNum).delegate.toInt()

}