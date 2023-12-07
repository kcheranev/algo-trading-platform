package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom

import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionSearchCommand
import ru.kcheranev.trading.domain.entity.TradeSessionSort
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.addAndCondition
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.addComparsionCondition

class CustomizedTradeSessionRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : CustomizedTradeSessionRepository {

    override fun search(command: TradeSessionSearchCommand): List<TradeSessionEntity> {
        val queryBuilder = StringBuilder()
        queryBuilder.append("SELECT * FROM trade_session")
        val conditionsBuilder = StringBuilder()
        if (command.id != null) {
            conditionsBuilder.addAndCondition("id = ${command.id.value}")
        }
        if (command.ticker != null) {
            conditionsBuilder.addAndCondition("ticker = ${command.ticker}")
        }
        if (command.instrumentId != null) {
            conditionsBuilder.addAndCondition("instrument_id = ${command.instrumentId}")
        }
        if (command.status != null) {
            conditionsBuilder.addAndCondition("status = ${command.status}")
        }
        if (command.startDate != null) {
            conditionsBuilder.addComparsionCondition("start_date", command.startDate)
        }
        if (command.candleInterval != null) {
            conditionsBuilder.addAndCondition("candle_interval = ${command.candleInterval}")
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
        return jdbcTemplate.query(queryBuilder.toString(), BeanPropertyRowMapper())
    }

    companion object {

        private const val DEFAULT_OFFSET = 0

        private const val DEFAULT_LIMIT = 10

    }

}