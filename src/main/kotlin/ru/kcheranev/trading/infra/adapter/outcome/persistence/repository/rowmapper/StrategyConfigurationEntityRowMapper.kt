package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.rowmapper

import org.postgresql.util.PGobject
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.converter.MapToJsonbReadConverter
import java.sql.ResultSet
import java.util.UUID

@Component
class StrategyConfigurationEntityRowMapper(
    private val mapToJsonbReadConverter: MapToJsonbReadConverter
) : RowMapper<StrategyConfigurationEntity> {

    override fun mapRow(rs: ResultSet, rowNum: Int) =
        StrategyConfigurationEntity(
            id = UUID.fromString(rs.getString("id")),
            name = rs.getString("name"),
            type = rs.getString("type"),
            candleInterval = CandleInterval.valueOf(rs.getString("candle_interval")),
            parameters = mapToJsonbReadConverter.convert(rs.getObject("parameters", PGobject::class.java))
        )

}