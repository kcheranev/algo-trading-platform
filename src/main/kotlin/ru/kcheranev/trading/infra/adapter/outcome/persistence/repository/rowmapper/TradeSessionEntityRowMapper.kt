package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.rowmapper

import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import java.sql.ResultSet

@Component
class TradeSessionEntityRowMapper : RowMapper<TradeSessionEntity> {

    override fun mapRow(rs: ResultSet, rowNum: Int) =
        TradeSessionEntity(
            id = rs.getLong("id"),
            ticker = rs.getString("ticker"),
            instrumentId = rs.getString("instrument_id"),
            status = TradeSessionStatus.valueOf(rs.getString("status")),
            startDate = rs.getTimestamp("start_date").toLocalDateTime(),
            candleInterval = CandleInterval.valueOf(rs.getString("candle_interval")),
            lotsQuantity = rs.getInt("lots_quantity"),
            lastEventDate = rs.getTimestamp("last_event_date").toLocalDateTime(),
            strategyConfigurationId = rs.getLong("strategy_configuration_id")
        )

}