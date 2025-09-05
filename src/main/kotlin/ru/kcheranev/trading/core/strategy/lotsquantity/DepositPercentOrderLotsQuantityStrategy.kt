package ru.kcheranev.trading.core.strategy.lotsquantity

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.error.DomainError
import ru.kcheranev.trading.core.error.NotEnoughMoneyOnDepositError
import ru.kcheranev.trading.core.error.OrderLotsQuantityCalculatingError
import ru.kcheranev.trading.core.port.outcome.broker.OperationServiceBrokerPort
import ru.kcheranev.trading.core.port.outcome.persistence.instrument.GetInstrumentByBrokerInstrumentIdCommand
import ru.kcheranev.trading.core.port.outcome.persistence.instrument.InstrumentPersistencePort
import ru.kcheranev.trading.core.util.validate
import ru.kcheranev.trading.domain.entity.TradeSession
import java.math.BigDecimal
import java.math.RoundingMode

const val DEPOSIT_PERCENT_STRATEGY_PARAMETER_NAME = "depositPercent"

@Component
class DepositPercentOrderLotsQuantityStrategy(
    private val instrumentPersistencePort: InstrumentPersistencePort,
    private val operationServiceBrokerPort: OperationServiceBrokerPort
) : OrderLotsQuantityStrategy {

    override val type = OrderLotsQuantityStrategyType.DEPOSIT_PERCENT

    private val factor = BigDecimal("1.1")

    override fun getLotsQuantity(tradeSession: TradeSession): Either<DomainError, Int> =
        either {
            val depositPercent = tradeSession.strategyParameters.getAsBigDecimal(DEPOSIT_PERCENT_STRATEGY_PARAMETER_NAME)
            validate {
                field("depositPercent") {
                    depositPercent.shouldNotBeNull()
                    depositPercent.shouldBeLessThan(BigDecimal("1.0"))
                }
            }
            val portfolio =
                operationServiceBrokerPort.getPortfolio()
                    .mapLeft { OrderLotsQuantityCalculatingError }
                    .bind()
            val instrument = instrumentPersistencePort.getByBrokerInstrumentId(GetInstrumentByBrokerInstrumentIdCommand(tradeSession.instrumentId))
            val fullLotAmount = tradeSession.lastCandleClosePrice() * instrument.lot.toBigDecimal() * factor
            val depositDependentLotsQuantity =
                (portfolio.totalPortfolioAmount * depositPercent).divide(fullLotAmount, 0, RoundingMode.DOWN).toInt()
            if (tradeSession.isMargin()) {
                return@either depositDependentLotsQuantity
            }
            if (portfolio.currencyAmount >= portfolio.totalPortfolioAmount * depositPercent) {
                depositDependentLotsQuantity
            } else {
                val lotsQuantity = portfolio.currencyAmount.divide(fullLotAmount, RoundingMode.DOWN).toInt()
                ensure(lotsQuantity > 0) { NotEnoughMoneyOnDepositError }
                lotsQuantity
            }
        }

}