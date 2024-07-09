package ru.kcheranev.trading.infra.adapter.income.web.rest.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAdjustAndAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeCommand
import ru.kcheranev.trading.domain.model.backtesting.PeriodStrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAdjustAndAnalyzeResult
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StrategyAdjustAndAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StrategyAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.PeriodStrategyAnalyzeResultDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.StrategyAdjustAndAnalyzeDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
interface BacktestingWebIncomeAdapterMapper {

    fun map(source: StrategyAnalyzeRequestDto): StrategyAnalyzeCommand

    fun map(source: StrategyAdjustAndAnalyzeRequestDto): StrategyAdjustAndAnalyzeCommand

    fun map(source: PeriodStrategyAnalyzeResult): PeriodStrategyAnalyzeResultDto

    fun map(source: StrategyAdjustAndAnalyzeResult): StrategyAdjustAndAnalyzeDto

}

val backtestingWebIncomeAdapterMapper: BacktestingWebIncomeAdapterMapper = Mappers.getMapper(
    BacktestingWebIncomeAdapterMapper::class.java
)