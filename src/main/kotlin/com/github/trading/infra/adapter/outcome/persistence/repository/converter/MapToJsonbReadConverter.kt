package com.github.trading.infra.adapter.outcome.persistence.repository.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper
import org.postgresql.util.PGobject
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component

@Component
@ReadingConverter
class MapToJsonbReadConverter(
    private val objectMapper: ObjectMapper
) : Converter<PGobject, MapWrapper> {

    override fun convert(pgObject: PGobject): MapWrapper {
        val source = pgObject.value ?: return MapWrapper(emptyMap())
        return MapWrapper(objectMapper.readValue(source))
    }

}