package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom

import org.springframework.jdbc.core.JdbcTemplate
import ru.kcheranev.trading.core.port.common.model.sort.TradeOrderSort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeOrderSearchCommand
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.addAndCondition
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.condition.ComparstionCondition
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.condition.EqualsCondition
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.rowmapper.TradeOrderEntityRowMapper

private const val DEFAULT_OFFSET = 0

private const val DEFAULT_LIMIT = 10

class CustomizedTradeOrderRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
    private val tradeOrderEntityRowMapper: TradeOrderEntityRowMapper
) : CustomizedTradeOrderRepository {

    override fun search(command: TradeOrderSearchCommand): List<TradeOrderEntity> {
        val queryBuilder = StringBuilder()
        queryBuilder.append("SELECT * FROM trade_order")
        val conditionsBuilder = StringBuilder()
        if (command.id != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("id", command.id.value))
        }
        if (command.ticker != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("ticker", command.ticker))
        }
        if (command.instrumentId != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("instrument_id", command.instrumentId))
        }
        if (command.date != null) {
            conditionsBuilder.addAndCondition(ComparstionCondition("date", command.date))
        }
        if (command.lotsQuantity != null) {
            conditionsBuilder.addAndCondition(ComparstionCondition("lots_quantity", command.lotsQuantity))
        }
        if (command.totalPrice != null) {
            conditionsBuilder.addAndCondition(ComparstionCondition("total_price", command.totalPrice))
        }
        if (command.direction != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("direction", command.direction))
        }
        if (command.tradeSessionId != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("trade_session_id", command.tradeSessionId.value))
        }
        if (conditionsBuilder.isNotEmpty()) {
            queryBuilder.append(" WHERE $conditionsBuilder")
        }
        if (command.sort != null) {
            val sortField =
                when (command.sort.field) {
                    TradeOrderSort.TICKER -> "ticker"
                    TradeOrderSort.DATE -> "date"
                    TradeOrderSort.TOTAL_PRICE -> "total_price"
                    TradeOrderSort.DIRECTION -> "direction"
                }
            queryBuilder.append(" ORDER BY $sortField ${command.sort.order}")
        }
        if (command.page != null) {
            queryBuilder.append(" LIMIT ${command.page.limit} OFFSET ${command.page.offset}")
        } else {
            queryBuilder.append(" LIMIT $DEFAULT_LIMIT OFFSET $DEFAULT_OFFSET")
        }
        return jdbcTemplate.query(queryBuilder.toString(), tradeOrderEntityRowMapper)
    }

}