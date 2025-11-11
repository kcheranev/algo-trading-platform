package com.github.trading.domain.model

import com.github.trading.common.date.DateSupplier
import com.github.trading.common.date.toMskLocalDateTime
import com.github.trading.core.config.TradingProperties.Companion.tradingProperties
import com.github.trading.core.strategy.rule.isSatisfiedByType
import com.github.trading.domain.mapper.domainModelMapper
import com.github.trading.domain.model.backtesting.Order
import com.github.trading.domain.model.backtesting.StrategyAnalyzeResult
import com.github.trading.domain.model.backtesting.Trade
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
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime

class TradeStrategy(
    private val series: BarSeries,
    val margin: Boolean,
    strategy: Strategy
) : Strategy by strategy {

    fun addBar(bar: Bar) {
        series.addBar(bar)
    }

    fun addBar(candle: Candle) {
        addBar(domainModelMapper.map(candle, series.barBuilder()))
    }

    fun shouldEnter() = shouldEnter(series.endIndex)

    fun shouldExit() = shouldExit(series.endIndex)

    fun shouldExit(currentPosition: Position?): Boolean {
        val index = series.endIndex
        return !isUnstableAt(index) && exitRule.isSatisfiedByType(index, currentPosition)
    }

    fun lastCandleDate(): LocalDateTime? =
        if (series.isEmpty) {
            null
        } else {
            series.lastBar
                .endTime
                .toMskLocalDateTime()
        }

    fun lastCandleClose(): BigDecimal? =
        if (series.isEmpty) {
            null
        } else {
            series.lastBar.closePrice.toBigDecimal()
        }

    fun isFreshCandleSeries(candleInterval: CandleInterval): Boolean {
        val lastCandleDate = lastCandleDate() ?: return false
        return Duration.between(lastCandleDate, DateSupplier.currentDateTime())
            .dividedBy(candleInterval.duration) <= tradingProperties.availableDelayedCandlesCount
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
        return StrategyAnalyzeResult.from(
            netProfit = ProfitCriterion().calculate(series, tradingRecord).toBigDecimal(),
            grossProfit = ProfitCriterion(true).calculate(series, tradingRecord).toBigDecimal(),
            netLoss = LossCriterion().calculate(series, tradingRecord).toBigDecimal(),
            grossLoss = LossCriterion(true).calculate(series, tradingRecord).toBigDecimal(),
            profitTradesCount = NumberOfWinningPositionsCriterion().calculate(series, tradingRecord).toInt(),
            losingTradesCount = NumberOfLosingPositionsCriterion().calculate(series, tradingRecord).toInt(),
            tradesCount = NumberOfPositionsCriterion().calculate(series, tradingRecord).toInt(),
            consecutiveProfitTradesCount = NumberOfConsecutivePositionsCriterion(AnalysisCriterion.PositionFilter.PROFIT)
                .calculate(series, tradingRecord).toInt(),
            consecutiveLosingTradesCount = NumberOfConsecutivePositionsCriterion(AnalysisCriterion.PositionFilter.LOSS)
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
            date = series.getBar(trade.index).beginTime.toMskLocalDateTime(),
            direction = TradeDirection.valueOf(trade.type.name),
            netPrice = trade.netPrice.toBigDecimal(),
            grossPrice = trade.pricePerAsset.toBigDecimal()
        )

}

private fun Num.toBigDecimal() = (this as DecimalNum).delegate

private fun Num.toInt() = (this as DecimalNum).delegate.toInt()