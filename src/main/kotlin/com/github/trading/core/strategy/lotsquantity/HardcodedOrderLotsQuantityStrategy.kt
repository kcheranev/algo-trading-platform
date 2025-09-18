package com.github.trading.core.strategy.lotsquantity

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.github.trading.core.error.AppError
import com.github.trading.core.error.NotEnoughMoneyOnDepositError
import com.github.trading.core.error.OrderLotsQuantityCalculatingError
import com.github.trading.core.port.outcome.broker.OperationServiceBrokerPort
import com.github.trading.core.port.outcome.persistence.instrument.GetInstrumentByBrokerInstrumentIdCommand
import com.github.trading.core.port.outcome.persistence.instrument.InstrumentPersistencePort
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType.HARDCODED
import com.github.trading.domain.entity.TradeSession
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

const val LOTS_QUANTITY_STRATEGY_PARAMETER_NAME = "lotsQuantity"

@Component
class HardcodedOrderLotsQuantityStrategy(
    private val instrumentPersistencePort: InstrumentPersistencePort,
    private val operationServiceBrokerPort: OperationServiceBrokerPort
) : OrderLotsQuantityStrategy {

    override val type = HARDCODED

    private val factor = BigDecimal("1.1")

    override fun getLotsQuantity(tradeSession: TradeSession): Either<AppError, Int> =
        either {
            val hardcodedLotsQuantity = tradeSession.strategyParameters.getAsInt(LOTS_QUANTITY_STRATEGY_PARAMETER_NAME).bind()
            if (tradeSession.isMargin()) {
                return@either hardcodedLotsQuantity
            }
            val instrument = instrumentPersistencePort.getByBrokerInstrumentId(GetInstrumentByBrokerInstrumentIdCommand(tradeSession.instrumentId))
            val currencyAmount =
                operationServiceBrokerPort.getPortfolio()
                    .mapLeft { OrderLotsQuantityCalculatingError }
                    .bind()
                    .currencyAmount
            val fullLotAmount = tradeSession.lastCandleClosePrice() * instrument.lot.toBigDecimal()
            if (currencyAmount > fullLotAmount * hardcodedLotsQuantity.toBigDecimal() * factor) {
                hardcodedLotsQuantity
            } else {
                val lotsQuantity = currencyAmount.divide(fullLotAmount * factor, 0, RoundingMode.DOWN).toInt()
                ensure(lotsQuantity > 0) { NotEnoughMoneyOnDepositError }
                lotsQuantity
            }
        }

}