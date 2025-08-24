package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom

import org.springframework.jdbc.core.JdbcTemplate
import ru.kcheranev.trading.core.port.model.sort.StrategyConfigurationSort
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.SearchStrategyConfigurationCommand
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.query.EqualsCondition
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.query.addAndCondition
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.rowmapper.StrategyConfigurationEntityRowMapper

private const val DEFAULT_OFFSET = 0

private const val DEFAULT_LIMIT = 10

class CustomizedStrategyConfigurationRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
    private val strategyConfigurationEntityRowMapper: StrategyConfigurationEntityRowMapper
) : CustomizedStrategyConfigurationRepository {

    override fun search(command: SearchStrategyConfigurationCommand): List<StrategyConfigurationEntity> {
        val queryBuilder = StringBuilder()
        queryBuilder.append("SELECT * FROM strategy_configuration")
        val conditionsBuilder = StringBuilder()
        val parameters = mutableListOf<Any>()
        if (command.id != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("id"))
            parameters.add(command.id.value)
        }
        if (command.type != null) {
            conditionsBuilder.addAndCondition(EqualsCondition("type"))
            parameters.add(command.type)
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
        return jdbcTemplate.query(
            queryBuilder.toString(),
            strategyConfigurationEntityRowMapper,
            *parameters.toTypedArray()
        )
    }

}