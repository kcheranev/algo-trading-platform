package com.github.trading.infra.adapter.outcome.persistence.repository.converter

import com.fasterxml.jackson.databind.ObjectMapper
import org.postgresql.util.PGobject
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper

@Component
@WritingConverter
class JsonbToMapWriteConverter(
    private val objectMapper: ObjectMapper
) : Converter<MapWrapper, PGobject> {

    override fun convert(source: MapWrapper): PGobject {
        val jsonObject = PGobject()
        jsonObject.type = "jsonb"
        jsonObject.value = objectMapper.writeValueAsString(source.value)
        return jsonObject
    }

}