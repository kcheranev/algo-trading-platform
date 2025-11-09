package com.github.trading.core.strategy.lotsquantity

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.github.trading.core.error.AppError
import com.github.trading.core.error.DomainError
import com.github.trading.core.port.outcome.broker.GetMaxLotsCommand
import com.github.trading.core.port.outcome.broker.OperationServiceBrokerPort
import com.github.trading.core.port.outcome.broker.OrderServiceBrokerPort
import com.github.trading.core.port.outcome.persistence.instrument.GetInstrumentByBrokerInstrumentIdCommand
import com.github.trading.core.port.outcome.persistence.instrument.InstrumentPersistencePort
import com.github.trading.core.util.Validator.Companion.validate
import com.github.trading.domain.entity.TradeSession
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min

const val DEPOSIT_PERCENT_STRATEGY_PARAMETER_NAME = "depositPercent"

@Component
class DepositPercentOrderLotsQuantityStrategy(
    private val orderServiceBrokerPort: OrderServiceBrokerPort,
    private val instrumentPersistencePort: InstrumentPersistencePort,
    private val operationServiceBrokerPort: OperationServiceBrokerPort
) : OrderLotsQuantityStrategy {

    override val type = OrderLotsQuantityStrategyType.DEPOSIT_PERCENT

    override fun getLotsQuantity(tradeSession: TradeSession): Either<AppError, Int> =
        either {
            val depositPercent = tradeSession.strategyParameters.getAsBigDecimal(DEPOSIT_PERCENT_STRATEGY_PARAMETER_NAME).bind()
            validate {
                field("depositPercent") {
                    depositPercent.shouldBeLessThanOrEquals(BigDecimal("1.0"))
                }
            }.bind()
            val portfolio =
                operationServiceBrokerPort.getPortfolio()
                    .mapLeft { DomainError.OrderLotsQuantityCalculatingError }
                    .bind()
            val instrument = instrumentPersistencePort.getByBrokerInstrumentId(GetInstrumentByBrokerInstrumentIdCommand(tradeSession.instrumentId))
            val fullLotAmount = tradeSession.lastCandleClosePrice() * instrument.lot.toBigDecimal()
            val depositDependentLotsQuantity = (portfolio.totalPortfolioAmount * depositPercent).divide(fullLotAmount, 0, RoundingMode.DOWN).toInt()
            val maxAvailableLots = orderServiceBrokerPort.getMaxLots(GetMaxLotsCommand(tradeSession.instrument)).bind()
            val resultLotsQuantity =
                if (tradeSession.isMargin()) {
                    min(depositDependentLotsQuantity, maxAvailableLots.sellMarginLimits.sellMaxLots)
                } else {
                    min(depositDependentLotsQuantity, maxAvailableLots.buyMarginLimits.buyMaxMarketLots)
                }
            ensure(resultLotsQuantity > 0) { DomainError.NotEnoughMoneyOnDepositError }
            resultLotsQuantity
        }

}