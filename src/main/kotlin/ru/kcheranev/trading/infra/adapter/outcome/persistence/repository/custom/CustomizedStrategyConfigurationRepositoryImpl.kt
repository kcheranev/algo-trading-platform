package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom

import org.springframework.jdbc.core.JdbcTemplate
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationSearchCommand
import ru.kcheranev.trading.domain.entity.StrategyConfigurationSort
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.addAndCondition
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.condition.EqualsCondition
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.rowmapper.StrategyConfigurationEntityRowMapper

private const val DEFAULT_OFFSET = 0

private const val DEFAULT_LIMIT = 10

class CustomizedStrategyConfigurationRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
    private val strategyConfigurationEntityRowMapper: StrategyConfigurationEntityRowMapper
) : CustomizedStrategyConfigurationRepository {

    override fun search(command: StrategyConfigurationSearchCommand): List<StrategyConfigurationEntity> {
        val queryBuilder = StringBuilder()
        queryBuilder.append("SELECT * FROM strategy_configuration")
        val conditionsBuilder = StringBuilder()
        if (command.id != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("id", command.id.value))
        }
        if (command.type != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("type", command.type))
        }
        if (command.candleInterval != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("candle_interval", command.candleInterval))
        }
        if (conditionsBuilder.isNotEmpty()) {
            queryBuilder.append(" WHERE $conditionsBuilder")
        }
        if (command.sort != null) {
            val sortField =
                when (command.sort.field) {
                    StrategyConfigurationSort.TYPE -> "type"
                    StrategyConfigurationSort.CANDLE_INTERVAL -> "candle_interval"
                }
            queryBuilder.append(" ORDER BY $sortField ${command.sort.order}")
        }
        if (command.page != null) {
            queryBuilder.append(" LIMIT ${command.page.limit} OFFSET ${command.page.offset}")
        } else {
            queryBuilder.append(" LIMIT $DEFAULT_LIMIT OFFSET $DEFAULT_OFFSET")
        }
        return jdbcTemplate.query(queryBuilder.toString(), strategyConfigurationEntityRowMapper)
    }

}