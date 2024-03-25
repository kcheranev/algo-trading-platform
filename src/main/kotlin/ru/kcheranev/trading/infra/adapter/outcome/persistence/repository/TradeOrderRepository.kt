package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.CustomizedTradeOrderRepository
import java.util.UUID

interface TradeOrderRepository : CrudRepository<TradeOrderEntity, UUID>, CustomizedTradeOrderRepository {
}