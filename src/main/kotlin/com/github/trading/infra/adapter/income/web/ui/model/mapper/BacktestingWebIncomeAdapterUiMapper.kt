package com.github.trading.infra.adapter.income.web.ui.model.mapper

import com.github.trading.core.port.income.backtesting.StrategyAnalyzeOnBrokerDataCommand
import com.github.trading.core.port.income.backtesting.StrategyAnalyzeOnStoredDataCommand
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.StrategyParameters
import com.github.trading.domain.model.backtesting.DailyStrategyAnalyzeResult
import com.github.trading.domain.model.backtesting.StrategyAnalyzeResult
import com.github.trading.domain.model.backtesting.StrategyParametersAnalyzeResult
import com.github.trading.infra.adapter.income.web.ui.model.request.CheckedValueUiDto
import com.github.trading.infra.adapter.income.web.ui.model.request.StrategyAnalyzeRequestUiDto
import com.github.trading.infra.adapter.income.web.ui.model.request.StrategyParameterUiDto
import com.github.trading.infra.adapter.income.web.ui.model.response.DailyStrategyAnalyzeResultUiDto
import com.github.trading.infra.adapter.income.web.ui.model.response.StrategyAnalyzeResultUiDto
import com.github.trading.infra.adapter.income.web.ui.model.response.StrategyParametersAnalyzeResultUiDto
import com.github.trading.infra.adapter.mapper.EntityIdMapper
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.mapstruct.factory.Mappers
import org.springframework.core.io.Resource

@Mapper(uses = [EntityIdMapper::class])
abstract class BacktestingWebIncomeAdapterUiMapper {

    @Mapping(
        target = "strategyParameters",
        source = "request.strategyParameters",
        qualifiedByName = ["mapNoMutableStrategyParameters"]
    )
    @Mapping(
        target = "mutableStrategyParameters",
        source = "request.strategyParameters",
        qualifiedByName = ["mapMutableStrategyParameters"]
    )
    abstract fun mapToStrategyAnalyzeOnBrokerDataCommand(
        request: StrategyAnalyzeRequestUiDto,
        instrument: Instrument
    ): StrategyAnalyzeOnBrokerDataCommand

    @Mapping(
        target = "strategyParameters",
        source = "request.strategyParameters",
        qualifiedByName = ["mapNoMutableStrategyParameters"]
    )
    @Mapping(
        target = "mutableStrategyParameters",
        source = "request.strategyParameters",
        qualifiedByName = ["mapMutableStrategyParameters"]
    )
    abstract fun mapToStrategyAnalyzeOnStoredDataCommand(
        request: StrategyAnalyzeRequestUiDto,
        candlesSeriesFile: Resource
    ): StrategyAnalyzeOnStoredDataCommand

    @Named("mapNoMutableStrategyParameters")
    fun mapNoMutableStrategyParameters(source: MutableMap<String, StrategyParameterUiDto>) =
        source.filter { !it.value.mutable }
            .mapValues { it.value.value!! }
            .let(::StrategyParameters)

    @Named("mapMutableStrategyParameters")
    fun mapMutableStrategyParameters(source: MutableMap<String, StrategyParameterUiDto>) =
        source.filter { it.value.mutable }
            .mapValues { it.value.value!! }
            .let(::StrategyParameters)

    fun mapCheckedValue(source: CheckedValueUiDto) = if (source.checked) source.value else null

    abstract fun map(source: StrategyAnalyzeResult): StrategyAnalyzeResultUiDto

    abstract fun map(source: DailyStrategyAnalyzeResult): DailyStrategyAnalyzeResultUiDto

    abstract fun map(source: StrategyParametersAnalyzeResult): StrategyParametersAnalyzeResultUiDto

}

val backtestingWebIncomeAdapterUiMapper: BacktestingWebIncomeAdapterUiMapper = Mappers.getMapper(
    BacktestingWebIncomeAdapterUiMapper::class.java
)