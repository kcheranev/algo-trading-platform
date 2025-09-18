package com.github.trading.infra.adapter.income.web.rest.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import com.github.trading.core.port.income.historiccandles.StoreHistoricCandlesCommand
import com.github.trading.infra.adapter.income.web.rest.model.request.StoreHistoricCandlesRequestDto

@Mapper
interface HistoricCandlesWebIncomeAdapterMapper {

    fun map(source: StoreHistoricCandlesRequestDto): StoreHistoricCandlesCommand

}

val historicCandlesWebIncomeAdapterMapper: HistoricCandlesWebIncomeAdapterMapper = Mappers.getMapper(
    HistoricCandlesWebIncomeAdapterMapper::class.java
)