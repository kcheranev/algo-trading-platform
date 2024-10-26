package ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyParametersAnalyzeCommand
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.backtesting.DailyStrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyParametersAnalyzeResult
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParameterUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParametersAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.DailyStrategyAnalyzeResultUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.StrategyAnalyzeResultUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.StrategyParametersAnalyzeResultUiDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
abstract class BacktestingWebIncomeAdapterUiMapper {

    @Mapping(target = "strategyParameters", qualifiedByName = ["mapStrategyParameters"])
    abstract fun map(source: StrategyAnalyzeRequestUiDto): StrategyAnalyzeCommand

    @Named("mapStrategyParameters")
    fun mapStrategyParameters(source: Map<String, Number>) = StrategyParameters(source)

    @Mapping(
        target = "strategyParameters",
        source = "strategyParameters",
        qualifiedByName = ["mapNoMutableStrategyParameters"]
    )
    @Mapping(
        target = "mutableStrategyParameters",
        source = "strategyParameters",
        qualifiedByName = ["mapMutableStrategyParameters"]
    )
    abstract fun map(source: StrategyParametersAnalyzeRequestUiDto): StrategyParametersAnalyzeCommand

    @Named("mapNoMutableStrategyParameters")
    fun mapNoMutableStrategyParameters(source: MutableMap<String, StrategyParameterUiDto>) =
        source.filter { it.value.mutable == false }
            .mapValues { it.value.value!! }
            .let { StrategyParameters(it) }

    @Named("mapMutableStrategyParameters")
    fun mapMutableStrategyParameters(source: MutableMap<String, StrategyParameterUiDto>) =
        source.filter { it.value.mutable == true }
            .mapValues { it.value.value!! }
            .let { StrategyParameters(it) }

    @Mapping(target = "results", source = ".", qualifiedByName = ["mapResults"])
    abstract fun map(source: StrategyAnalyzeResult): StrategyAnalyzeResultUiDto

    @Named("mapResults")
    fun mapResults(source: StrategyAnalyzeResult) =
        source.splitByDay()
            .mapValues { map(it.value) }

    abstract fun map(source: DailyStrategyAnalyzeResult): DailyStrategyAnalyzeResultUiDto

    abstract fun map(source: StrategyParametersAnalyzeResult): StrategyParametersAnalyzeResultUiDto

}

val backtestingWebIncomeAdapterUiMapper: BacktestingWebIncomeAdapterUiMapper = Mappers.getMapper(
    BacktestingWebIncomeAdapterUiMapper::class.java
)