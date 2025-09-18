package com.github.trading.infra.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import com.github.trading.infra.adapter.outcome.persistence.repository.converter.JsonbToMapWriteConverter
import com.github.trading.infra.adapter.outcome.persistence.repository.converter.MapToJsonbReadConverter

@Configuration
class JdbcConfiguration(
    private val mapToJsonbReadConverter: MapToJsonbReadConverter,
    private val jsonbToMapWriteConverter: JsonbToMapWriteConverter
) : AbstractJdbcConfiguration() {

    override fun userConverters(): List<*> {
        return listOf(mapToJsonbReadConverter, jsonbToMapWriteConverter)
    }

}