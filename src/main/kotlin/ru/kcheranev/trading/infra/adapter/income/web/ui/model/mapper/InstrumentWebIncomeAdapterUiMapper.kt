package ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.instrument.CreateInstrumentCommand
import ru.kcheranev.trading.domain.entity.Instrument
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.CreateInstrumentRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.InstrumentUiResponseDto

@Mapper
interface InstrumentWebIncomeAdapterUiMapper {

    @Mapping(source = "id.value", target = "id")
    fun map(source: Instrument): InstrumentUiResponseDto

    fun map(source: CreateInstrumentRequestUiDto): CreateInstrumentCommand

}

val instrumentWebIncomeAdapterUiMapper: InstrumentWebIncomeAdapterUiMapper = Mappers.getMapper(
    InstrumentWebIncomeAdapterUiMapper::class.java
)