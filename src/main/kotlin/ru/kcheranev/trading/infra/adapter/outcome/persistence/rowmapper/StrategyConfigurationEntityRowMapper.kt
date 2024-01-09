package ru.kcheranev.trading.infra.adapter.outcome.persistence.rowmapper

import org.postgresql.util.PGobject
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.persistence.converter.MapToJsonbReadConverter
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import java.sql.ResultSet

@Component
class StrategyConfigurationEntityRowMapper(
    private val mapToJsonbReadConverter: MapToJsonbReadConverter
) : RowMapper<StrategyConfigurationEntity> {

    override fun mapRow(rs: ResultSet, rowNum: Int) =
        StrategyConfigurationEntity(
            id = rs.getLong("id"),
            type = rs.getString("type"),
            initCandleAmount = rs.getInt("init_candle_amount"),
            candleInterval = CandleInterval.valueOf(rs.getString("candle_interval")),
            params = mapToJsonbReadConverter.convert(rs.getObject("params", PGobject::class.java))
        )

}