package com.github.trading.infra.adapter.outcome.persistence.repository.rowmapper

import com.github.trading.domain.model.TradeDirection
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.UUID

@Component
class TradeOrderEntityRowMapper : RowMapper<TradeOrderEntity> {

    override fun mapRow(rs: ResultSet, rowNum: Int) =
        TradeOrderEntity(
            id = UUID.fromString(rs.getString("id")),
            ticker = rs.getString("ticker"),
            instrumentId = rs.getString("instrument_id"),
            date = rs.getTimestamp("date").toLocalDateTime(),
            lotsQuantity = rs.getInt("lots_quantity"),
            totalPrice = rs.getBigDecimal("total_price"),
            executedCommission = rs.getBigDecimal("executed_commission"),
            direction = TradeDirection.valueOf(rs.getString("direction")),
            tradeSessionId = UUID.fromString(rs.getString("trade_session_id"))
        )

}