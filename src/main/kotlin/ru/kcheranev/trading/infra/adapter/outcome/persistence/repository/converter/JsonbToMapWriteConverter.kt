package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.converter

import com.fasterxml.jackson.databind.ObjectMapper
import org.postgresql.util.PGobject
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper

@Component
@WritingConverter
class JsonbToMapWriteConverter(
    private val objectMapper: ObjectMapper
) : Converter<MapWrapper<String, Any>, PGobject> {

    override fun convert(source: MapWrapper<String, Any>): PGobject {
        val jsonObject = PGobject()
        jsonObject.type = "jsonb"
        jsonObject.value = objectMapper.writeValueAsString(source.value)
        return jsonObject
    }

}