package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.CustomizedStrategyConfigurationRepository
import java.util.UUID

interface StrategyConfigurationRepository : CrudRepository<StrategyConfigurationEntity, UUID>,
    CustomizedStrategyConfigurationRepository {
}