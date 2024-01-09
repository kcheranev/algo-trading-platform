package ru.kcheranev.trading.infra.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import ru.kcheranev.trading.infra.adapter.outcome.persistence.converter.JsonbToMapWriteConverter
import ru.kcheranev.trading.infra.adapter.outcome.persistence.converter.MapToJsonbReadConverter

@Configuration
class JdbcConfiguration(
    private val mapToJsonbReadConverter: MapToJsonbReadConverter,
    private val jsonbToMapWriteConverter: JsonbToMapWriteConverter
) : AbstractJdbcConfiguration() {

    override fun userConverters(): List<*> {
        return listOf(mapToJsonbReadConverter, jsonbToMapWriteConverter)
    }

}