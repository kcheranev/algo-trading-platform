package com.github.trading.infra.adapter.outcome.persistence.repository.custom

import com.github.trading.core.port.model.sort.TradeOrderSort
import com.github.trading.core.port.outcome.persistence.tradeorder.SearchTradeOrderCommand
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import com.github.trading.infra.adapter.outcome.persistence.repository.custom.query.ComparstionCondition
import com.github.trading.infra.adapter.outcome.persistence.repository.custom.query.EqualsCondition
import com.github.trading.infra.adapter.outcome.persistence.repository.custom.query.addAndCondition
import com.github.trading.infra.adapter.outcome.persistence.repository.rowmapper.TradeOrderEntityRowMapper
import org.springframework.jdbc.core.JdbcTemplate

private const val DEFAULT_OFFSET = 0

private const val DEFAULT_LIMIT = 10

class CustomizedTradeOrderRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
    private val tradeOrderEntityRowMapper: TradeOrderEntityRowMapper
) : CustomizedTradeOrderRepository {

    override fun search(command: SearchTradeOrderCommand): List<TradeOrderEntity> {
        val queryBuilder = StringBuilder()
        queryBuilder.append("SELECT * FROM trade_order")
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
        if (command.date != null) {
            conditionsBuilder.addAndCondition(ComparstionCondition("date", command.date.comparsion))
            parameters.add(command.date.value)
        }
        if (command.lotsQuantity != null) {
            conditionsBuilder.addAndCondition(ComparstionCondition("lots_quantity", command.lotsQuantity.comparsion))
            parameters.add(command.lotsQuantity.value)
        }
        if (command.totalPrice != null) {
            conditionsBuilder.addAndCondition(ComparstionCondition("total_price", command.totalPrice.comparsion))
            parameters.add(command.totalPrice.value)
        }
        if (command.direction != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("direction"))
            parameters.add(command.direction.name)
        }
        if (command.tradeSessionId != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("trade_session_id"))
            parameters.add(command.tradeSessionId.value)
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
        return jdbcTemplate.query(queryBuilder.toString(), tradeOrderEntityRowMapper, *parameters.toTypedArray())
    }

}