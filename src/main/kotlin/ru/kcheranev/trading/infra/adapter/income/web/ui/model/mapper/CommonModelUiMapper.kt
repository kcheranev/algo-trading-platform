package ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.domain.entity.Instrument
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.common.InstrumentUiDto

@Mapper
interface CommonModelUiMapper {

    @Mapping(source = "id.value", target = "id")
    fun map(source: Instrument): InstrumentUiDto

}

val commonModelUiMapper: CommonModelUiMapper = Mappers.getMapper(
    CommonModelUiMapper::class.java
)