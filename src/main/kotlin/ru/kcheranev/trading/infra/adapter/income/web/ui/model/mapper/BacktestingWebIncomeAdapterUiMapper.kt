package ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAdjustAndAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeCommand
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.backtesting.PeriodStrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAdjustAndAnalyzeResult
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAdjustAndAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParamDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.PeriodStrategyAnalyzeResultUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.StrategyAdjustAndAnalyzeUiDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
abstract class BacktestingWebIncomeAdapterUiMapper {

    @Mapping(target = "strategyParams", qualifiedByName = ["mapStrategyParameters"])
    abstract fun map(source: StrategyAnalyzeRequestUiDto): StrategyAnalyzeCommand

    @Mapping(target = "strategyParams", qualifiedByName = ["mapStrategyParameters"])
    @Mapping(target = "mutableStrategyParams", qualifiedByName = ["mapStrategyParameters"])
    abstract fun map(source: StrategyAdjustAndAnalyzeRequestUiDto): StrategyAdjustAndAnalyzeCommand

    abstract fun map(source: PeriodStrategyAnalyzeResult): PeriodStrategyAnalyzeResultUiDto

    abstract fun map(source: StrategyAdjustAndAnalyzeResult): StrategyAdjustAndAnalyzeUiDto

    @Named("mapStrategyParameters")
    fun mapStrategyParameters(source: List<StrategyParamDto>) =
        StrategyParameters(
            source.associate { it.name!! to it.value!! }
        )

}

val backtestingWebIncomeAdapterUiMapper: BacktestingWebIncomeAdapterUiMapper = Mappers.getMapper(
    BacktestingWebIncomeAdapterUiMapper::class.java
)