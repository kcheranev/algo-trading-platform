package com.github.trading.infra.adapter.income.web.rest.model.mapper

import com.github.trading.core.port.income.backtesting.StrategyAnalyzeOnBrokerDataCommand
import com.github.trading.domain.model.backtesting.DailyStrategyAnalyzeResult
import com.github.trading.domain.model.backtesting.StrategyAnalyzeResult
import com.github.trading.domain.model.backtesting.StrategyParametersAnalyzeResult
import com.github.trading.infra.adapter.income.web.rest.model.request.StrategyAnalyzeRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.response.DailyStrategyAnalyzeResultDto
import com.github.trading.infra.adapter.income.web.rest.model.response.StrategyAnalyzeResultDto
import com.github.trading.infra.adapter.income.web.rest.model.response.StrategyParametersAnalyzeResultDto
import com.github.trading.infra.adapter.mapper.EntityIdMapper
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

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