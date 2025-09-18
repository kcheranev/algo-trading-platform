package com.github.trading.infra.adapter.outcome.persistence.repository

import com.github.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import com.github.trading.infra.adapter.outcome.persistence.repository.custom.CustomizedTradeOrderRepository
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface TradeOrderRepository : CrudRepository<TradeOrderEntity, UUID>, CustomizedTradeOrderRepository {
}