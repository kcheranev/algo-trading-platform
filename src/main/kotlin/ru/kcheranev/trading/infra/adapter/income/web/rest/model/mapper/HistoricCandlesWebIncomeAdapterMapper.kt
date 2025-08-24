package ru.kcheranev.trading.infra.adapter.income.web.rest.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.historiccandles.StoreHistoricCandlesCommand
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StoreHistoricCandlesRequestDto

@Mapper
interface HistoricCandlesWebIncomeAdapterMapper {

    fun map(source: StoreHistoricCandlesRequestDto): StoreHistoricCandlesCommand

}

val historicCandlesWebIncomeAdapterMapper: HistoricCandlesWebIncomeAdapterMapper = Mappers.getMapper(
    HistoricCandlesWebIncomeAdapterMapper::class.java
)