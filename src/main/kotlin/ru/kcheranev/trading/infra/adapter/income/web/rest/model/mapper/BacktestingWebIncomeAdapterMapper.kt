package ru.kcheranev.trading.infra.adapter.income.web.rest.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeOnBrokerDataCommand
import ru.kcheranev.trading.domain.model.backtesting.DailyStrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyParametersAnalyzeResult
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StrategyAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.DailyStrategyAnalyzeResultDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.StrategyAnalyzeResultDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.StrategyParametersAnalyzeResultDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
abstract class BacktestingWebIncomeAdapterMapper {

    abstract fun map(source: StrategyAnalyzeRequestDto): StrategyAnalyzeOnBrokerDataCommand

    abstract fun map(source: StrategyAnalyzeResult): StrategyAnalyzeResultDto

    abstract fun map(source: DailyStrategyAnalyzeResult): DailyStrategyAnalyzeResultDto

    abstract fun map(source: StrategyParametersAnalyzeResult): StrategyParametersAnalyzeResultDto

}

val backtestingWebIncomeAdapterMapper: BacktestingWebIncomeAdapterMapper = Mappers.getMapper(
    BacktestingWebIncomeAdapterMapper::class.java
)