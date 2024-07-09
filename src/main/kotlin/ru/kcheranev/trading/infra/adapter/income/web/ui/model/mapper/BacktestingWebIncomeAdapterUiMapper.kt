package ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAdjustAndAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeCommand
import ru.kcheranev.trading.domain.model.backtesting.PeriodStrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAdjustAndAnalyzeResult
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAdjustAndAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.PeriodStrategyAnalyzeResultUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.StrategyAdjustAndAnalyzeUiDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
interface BacktestingWebIncomeAdapterUiMapper {

    fun map(source: StrategyAnalyzeRequestUiDto): StrategyAnalyzeCommand

    fun map(source: StrategyAdjustAndAnalyzeRequestUiDto): StrategyAdjustAndAnalyzeCommand

    fun map(source: PeriodStrategyAnalyzeResult): PeriodStrategyAnalyzeResultUiDto

    fun map(source: StrategyAdjustAndAnalyzeResult): StrategyAdjustAndAnalyzeUiDto

}

val backtestingWebIncomeAdapterUiMapper: BacktestingWebIncomeAdapterUiMapper = Mappers.getMapper(
    BacktestingWebIncomeAdapterUiMapper::class.java
)