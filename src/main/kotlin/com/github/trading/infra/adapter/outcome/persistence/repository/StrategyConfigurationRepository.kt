package com.github.trading.infra.adapter.outcome.persistence.repository

import com.github.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import com.github.trading.infra.adapter.outcome.persistence.repository.custom.CustomizedStrategyConfigurationRepository
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface StrategyConfigurationRepository : CrudRepository<StrategyConfigurationEntity, UUID>,
    CustomizedStrategyConfigurationRepository {
}