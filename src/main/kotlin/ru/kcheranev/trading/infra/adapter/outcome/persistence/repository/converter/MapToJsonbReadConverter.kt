package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.postgresql.util.PGobject
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper

@Component
@ReadingConverter
class MapToJsonbReadConverter(
    private val objectMapper: ObjectMapper
) : Converter<PGobject, MapWrapper<String, Int>> {

    override fun convert(pgObject: PGobject): MapWrapper<String, Int> {
        val source = pgObject.value ?: return MapWrapper(emptyMap())
        return MapWrapper(objectMapper.readValue(source))
    }

}