package ru.kcheranev.trading.domain.entity

import ru.kcheranev.trading.common.DateSupplier
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class TradeOrder(
    val id: TradeOrderId?,
    val ticker: String,
    val instrumentId: String,
    val date: LocalDateTime,
    val lotsQuantity: Int,
    val totalPrice: BigDecimal,
    val executedCommission: BigDecimal,
    val direction: TradeDirection,
    val strategyConfigurationId: StrategyConfigurationId
) : AbstractAggregateRoot() {

    companion object {

        fun create(
            ticker: String,
            instrumentId: String,
            lotsQuantity: Int,
            totalPrice: BigDecimal,
            executedCommission: BigDecimal,
            direction: TradeDirection,
            strategyConfigurationId: StrategyConfigurationId,
            dateSupplier: DateSupplier
        ): TradeOrder =
            TradeOrder(
                id = null,
                ticker = ticker,
                instrumentId = instrumentId,
                date = dateSupplier.currentDate(),
                lotsQuantity = lotsQuantity,
                totalPrice = totalPrice,
                executedCommission = executedCommission,
                direction = direction,
                strategyConfigurationId = strategyConfigurationId
            )

    }

}

data class TradeOrderId(
    val value: UUID
)

enum class TradeDirection {

    BUY, SELL

}