package com.github.trading.infra.adapter.outcome.persistence.repository.custom

import com.github.trading.core.port.model.sort.TradeSessionSort
import com.github.trading.core.port.outcome.persistence.tradesession.SearchTradeSessionCommand
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import com.github.trading.infra.adapter.outcome.persistence.repository.custom.query.EqualsCondition
import com.github.trading.infra.adapter.outcome.persistence.repository.custom.query.addAndCondition
import com.github.trading.infra.adapter.outcome.persistence.repository.rowmapper.TradeSessionEntityRowMapper
import org.springframework.jdbc.core.JdbcTemplate

private const val DEFAULT_OFFSET = 0

private const val DEFAULT_LIMIT = 10

class CustomizedTradeSessionRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
    private val tradeSessionEntityRowMapper: TradeSessionEntityRowMapper
) : CustomizedTradeSessionRepository {

    override fun search(command: SearchTradeSessionCommand): List<TradeSessionEntity> {
        val queryBuilder = StringBuilder()
        queryBuilder.append("SELECT * FROM trade_session")
        val conditionsBuilder = StringBuilder()
        val parameters = mutableListOf<Any>()
        if (command.id != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("id"))
            parameters.add(command.id.value)
        }
        if (command.ticker != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("ticker"))
            parameters.add(command.ticker)
        }
        if (command.instrumentId != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("instrument_id"))
            parameters.add(command.instrumentId)
        }
        if (command.status != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("status"))
            parameters.add(command.status.name)
        }
        if (command.candleInterval != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("candle_interval"))
            parameters.add(command.candleInterval.name)
        }
        if (conditionsBuilder.isNotEmpty()) {
            queryBuilder.append(" WHERE $conditionsBuilder")
        }
        if (command.sort != null) {
            val sortField =
                when (command.sort.field) {
                    TradeSessionSort.TICKER -> "ticker"
                    TradeSessionSort.STATUS -> "status"
                    TradeSessionSort.START_DATE -> "start_date"
                    TradeSessionSort.CANDLE_INTERVAL -> "candle_interval"
                }
            queryBuilder.append(" ORDER BY $sortField ${command.sort.order}")
        }
        if (command.page != null) {
            queryBuilder.append(" LIMIT ${command.page.limit} OFFSET ${command.page.offset}")
        } else {
            queryBuilder.append(" LIMIT $DEFAULT_LIMIT OFFSET $DEFAULT_OFFSET")
        }
        return jdbcTemplate.query(queryBuilder.toString(), tradeSessionEntityRowMapper, *parameters.toTypedArray())
    }

}