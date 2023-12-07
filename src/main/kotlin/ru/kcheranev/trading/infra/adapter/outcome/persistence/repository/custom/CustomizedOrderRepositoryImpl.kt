package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom

import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import ru.kcheranev.trading.core.port.outcome.persistence.OrderSearchCommand
import ru.kcheranev.trading.domain.entity.OrderSort
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.OrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.addAndCondition
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.addComparsionCondition

class CustomizedOrderRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : CustomizedOrderRepository {

    override fun search(command: OrderSearchCommand): List<OrderEntity> {
        val queryBuilder = StringBuilder()
        queryBuilder.append("SELECT * FROM order")
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
        if (command.date != null) {
            conditionsBuilder.addComparsionCondition("date", command.date)
        }
        if (command.quantity != null) {
            conditionsBuilder.addComparsionCondition("quantity", command.quantity)
        }
        if (command.price != null) {
            conditionsBuilder.addComparsionCondition("price", command.price)
        }
        if (command.direction != null) {
            conditionsBuilder.addAndCondition("direction = ${command.direction}")
        }
        if (command.tradeSessionId != null) {
            conditionsBuilder.addAndCondition("trade_session_id = ${command.tradeSessionId.value}")
        }
        if (conditionsBuilder.isNotEmpty()) {
            queryBuilder.append(" WHERE $conditionsBuilder")
        }
        if (command.sort != null) {
            val sortField =
                when (command.sort.field) {
                    OrderSort.TICKER -> "ticker"
                    OrderSort.DATE -> "date"
                    OrderSort.PRICE -> "price"
                    OrderSort.DIRECTION -> "direction"
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