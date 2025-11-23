package com.github.trading.core.port.income.backtesting

import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.backtesting.MutableParameter
import com.github.trading.domain.model.backtesting.ProfitTypeSort
import com.github.trading.domain.model.backtesting.StrategyParametersMutation
import org.springframework.core.io.Resource
import java.math.BigDecimal
import java.time.LocalDate

data class StrategyAnalyzeOnBrokerDataCommand(
    val strategyType: String,
    val strategyParameters: Map<String, Number>,
    val mutableStrategyParameters: Map<String, MutableParameter>,
    val parametersMutation: StrategyParametersMutation,
    val resultFilter: StrategyAnalyzeResultFilter?,
    val profitTypeSort: ProfitTypeSort?,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val from: LocalDate,
    val to: LocalDate
)

data class StrategyAnalyzeOnStoredDataCommand(
    val strategyType: String,
    val strategyParameters: Map<String, Number>,
    val mutableStrategyParameters: Map<String, MutableParameter>,
    val parametersMutation: StrategyParametersMutation,
    val resultFilter: StrategyAnalyzeResultFilter?,
    val profitTypeSort: ProfitTypeSort?,
    val candlesSeriesFile: Resource
)

data class StrategyAnalyzeResultFilter(
    val resultsLimit: Int?,
    val minProfitLossTradesRatio: BigDecimal?,
    val tradesByDayCountFactor: BigDecimal?
)

