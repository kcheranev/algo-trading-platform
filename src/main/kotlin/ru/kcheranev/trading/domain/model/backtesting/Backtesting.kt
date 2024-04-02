package ru.kcheranev.trading.domain.model.backtesting

import org.ta4j.core.AnalysisCriterion.PositionFilter.LOSS
import org.ta4j.core.AnalysisCriterion.PositionFilter.PROFIT
import org.ta4j.core.BarSeries
import org.ta4j.core.BarSeriesManager
import org.ta4j.core.BaseBarSeriesBuilder
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
import ru.kcheranev.trading.core.strategy.StrategyFactory
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters

class Backtesting(
    val ticker: String,
    val candleInterval: CandleInterval,
    val candles: List<Candle>
) {

    fun analyzeStrategy(
        strategyFactory: StrategyFactory,
        params: StrategyParameters
    ): StrategyAnalyzeResult {
        val series: BarSeries = BaseBarSeriesBuilder()
            .withName("Trade session: ticker=$ticker, candleInterval=$candleInterval")
            .build()
        candles.forEach { series.addBar(domainModelMapper.map(it)) }
        val strategy = strategyFactory.initStrategy(params, series)
        val seriesManager = BarSeriesManager(series)
        val tradingRecord = seriesManager.run(strategy)
        return StrategyAnalyzeResult(
            averageLoss = AverageLossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            averageProfit = AverageProfitCriterion().calculate(series, tradingRecord).toBigDecimal(),
            enterAndHoldReturn = EnterAndHoldReturnCriterion()
                .calculate(series, tradingRecord).toBigDecimal(),
            netLoss = NetLossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            grossLoss = NetLossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            maximumDrawdown = MaximumDrawdownCriterion().calculate(series, tradingRecord).toBigDecimal(),
            numberOfBars = NumberOfBarsCriterion().calculate(series, tradingRecord).toInt(),
            numberOfConsecutiveProfitPositions = NumberOfConsecutivePositionsCriterion(PROFIT)
                .calculate(series, tradingRecord).toInt(),
            numberOfConsecutiveLosingPositions = NumberOfConsecutivePositionsCriterion(LOSS)
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
            profitLossRatio = ProfitLossRatioCriterion().calculate(series, tradingRecord).toBigDecimal()
        )
    }

    private fun Num.toBigDecimal() = (this as DecimalNum).delegate

    private fun Num.toInt() = (this as DecimalNum).delegate.toInt()

}