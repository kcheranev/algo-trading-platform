package ru.kcheranev.trading.infra.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import ru.kcheranev.trading.infra.adapter.outcome.persistence.converter.JsonbToMapWriteConverter
import ru.kcheranev.trading.infra.adapter.outcome.persistence.converter.MapToJsonbReadConverter

@Configuration
class JdbcConfiguration(private val objectMapper: ObjectMapper) : AbstractJdbcConfiguration() {

    override fun userConverters(): List<*> {
        return listOf(MapToJsonbReadConverter(objectMapper), JsonbToMapWriteConverter(objectMapper))
    }

}