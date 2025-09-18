package com.github.trading.infra.adapter.income.web.ui.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers
import com.github.trading.core.port.income.instrument.CreateInstrumentCommand
import com.github.trading.domain.entity.Instrument
import com.github.trading.infra.adapter.income.web.ui.model.request.CreateInstrumentRequestUiDto
import com.github.trading.infra.adapter.income.web.ui.model.response.InstrumentUiResponseDto

@Mapper
interface InstrumentWebIncomeAdapterUiMapper {

    @Mapping(source = "id.value", target = "id")
    fun map(source: Instrument): InstrumentUiResponseDto

    fun map(source: CreateInstrumentRequestUiDto): CreateInstrumentCommand

}

val instrumentWebIncomeAdapterUiMapper: InstrumentWebIncomeAdapterUiMapper = Mappers.getMapper(
    InstrumentWebIncomeAdapterUiMapper::class.java
)