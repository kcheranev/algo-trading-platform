package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity

interface StrategyConfigurationRepository : CrudRepository<StrategyConfigurationEntity, Long> {
}