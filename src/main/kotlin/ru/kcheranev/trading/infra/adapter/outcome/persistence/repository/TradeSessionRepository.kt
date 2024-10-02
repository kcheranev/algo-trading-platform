package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.CustomizedTradeSessionRepository
import java.util.UUID

interface TradeSessionRepository : CrudRepository<TradeSessionEntity, UUID>, CustomizedTradeSessionRepository {

    @Query(
        "SELECT * FROM trade_session WHERE instrument_id = :instrumentId AND candle_interval = :candleInterval " +
                "AND status IN ('WAITING', 'IN_POSITION')"
    )
    fun getReadyForOrderTradeSessions(
        @Param("instrumentId") instrumentId: String,
        @Param("candleInterval") candleInterval: CandleInterval
    ): List<TradeSessionEntity>

}