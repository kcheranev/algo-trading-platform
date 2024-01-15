package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.rowmapper

import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import ru.kcheranev.trading.domain.entity.TradeDirection
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import java.sql.ResultSet

@Component
class TradeOrderEntityRowMapper : RowMapper<TradeOrderEntity> {

    override fun mapRow(rs: ResultSet, rowNum: Int) =
        TradeOrderEntity(
            id = rs.getLong("id"),
            ticker = rs.getString("ticker"),
            instrumentId = rs.getString("instrument_id"),
            date = rs.getTimestamp("date").toLocalDateTime(),
            lotsQuantity = rs.getInt("lots_quantity"),
            price = rs.getBigDecimal("price"),
            direction = TradeDirection.valueOf(rs.getString("direction")),
            tradeSessionId = rs.getLong("trade_session_id")
        )

}