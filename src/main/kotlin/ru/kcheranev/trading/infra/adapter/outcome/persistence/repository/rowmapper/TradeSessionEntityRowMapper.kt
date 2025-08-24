package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.rowmapper

import org.postgresql.util.PGobject
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.converter.MapToJsonbReadConverter
import java.sql.ResultSet
import java.util.UUID

@Component
class TradeSessionEntityRowMapper(
    private val mapToJsonbReadConverter: MapToJsonbReadConverter
) : RowMapper<TradeSessionEntity> {

    override fun mapRow(rs: ResultSet, rowNum: Int) =
        TradeSessionEntity(
            id = UUID.fromString(rs.getString("id")),
            ticker = rs.getString("ticker"),
            instrumentId = rs.getString("instrument_id"),
            status = TradeSessionStatus.valueOf(rs.getString("status")),
            candleInterval = CandleInterval.valueOf(rs.getString("candle_interval")),
            orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.valueOf(rs.getString("order_lots_quantity_strategy_type")),
            positionLotsQuantity = rs.getInt("position_lots_quantity"),
            positionAveragePrice = rs.getBigDecimal("position_average_price"),
            strategyType = rs.getString("strategy_type"),
            strategyParameters = mapToJsonbReadConverter.convert(
                rs.getObject(
                    "strategy_parameters",
                    PGobject::class.java
                )
            )
        )

}