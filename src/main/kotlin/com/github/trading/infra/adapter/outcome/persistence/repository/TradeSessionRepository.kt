package com.github.trading.infra.adapter.outcome.persistence.repository

import com.github.trading.domain.model.CandleInterval
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import com.github.trading.infra.adapter.outcome.persistence.repository.custom.CustomizedTradeSessionRepository
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface TradeSessionRepository : CrudRepository<TradeSessionEntity, UUID>, CustomizedTradeSessionRepository {

    @Query(
        "SELECT * FROM trade_session WHERE status IN ('WAITING', 'IN_POSITION')"
    )
    fun getReadyForOrderTradeSessions(): List<TradeSessionEntity>

    @Query(
        "SELECT * FROM trade_session WHERE instrument_id = :instrumentId AND candle_interval = :candleInterval " +
                "AND status IN ('WAITING', 'IN_POSITION')"
    )
    fun getReadyForOrderTradeSessions(
        @Param("instrumentId") instrumentId: String,
        @Param("candleInterval") candleInterval: CandleInterval
    ): List<TradeSessionEntity>

    @Query(
        "SELECT EXISTS (SELECT FROM trade_session WHERE instrument_id = :instrumentId AND candle_interval = :candleInterval " +
                "AND status IN ('WAITING', 'IN_POSITION'))"
    )
    fun isReadyForOrderTradeSessionExists(
        @Param("instrumentId") instrumentId: String,
        @Param("candleInterval") candleInterval: CandleInterval
    ): Boolean

}