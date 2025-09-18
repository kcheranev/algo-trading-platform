package com.github.trading.test.unit.lotsquantity

import arrow.core.right
import com.github.trading.core.error.NotEnoughMoneyOnDepositError
import com.github.trading.core.port.outcome.broker.OperationServiceBrokerPort
import com.github.trading.core.port.outcome.persistence.instrument.InstrumentPersistencePort
import com.github.trading.core.strategy.lotsquantity.HardcodedOrderLotsQuantityStrategy
import com.github.trading.core.strategy.lotsquantity.LOTS_QUANTITY_STRATEGY_PARAMETER_NAME
import com.github.trading.domain.entity.Instrument
import com.github.trading.domain.entity.InstrumentId
import com.github.trading.domain.entity.TradeSession
import com.github.trading.domain.model.Portfolio
import com.github.trading.domain.model.StrategyParameters
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.util.UUID

class HardcodedOrderLotsQuantityStrategyTest : FreeSpec({

    "should get lots quantity" - {
        data class TestParameters(
            val totalPortfolioAmount: BigDecimal,
            val currencyAmount: BigDecimal,
            val lotsQuantity: Int
        )
        withData(
            nameFn = { "total portfolio amount = ${it.totalPortfolioAmount}, currency amount = ${it.currencyAmount}, lots quantity = ${it.lotsQuantity}" },
            TestParameters(BigDecimal("5000.0"), BigDecimal("2500.0"), 3),
            TestParameters(BigDecimal("5000.0"), BigDecimal("1500.0"), 2),
            TestParameters(BigDecimal("5000.0"), BigDecimal("1650.0"), 3)
        ) { (totalPortfolioAmount, currencyAmount, lotsQuantity) ->
            //given
            val instrumentPersistencePort =
                mockk<InstrumentPersistencePort> {
                    every { getByBrokerInstrumentId(any()) } returns
                            Instrument(
                                id = InstrumentId(UUID.randomUUID()),
                                name = "Сбербанк",
                                ticker = "SBER",
                                lot = 5,
                                brokerInstrumentId = "12345"
                            )
                }
            val operationServiceBrokerPort =
                mockk<OperationServiceBrokerPort> {
                    every { getPortfolio() } returns Portfolio(currencyAmount, BigDecimal.ZERO, totalPortfolioAmount).right()
                }
            val tradeSession =
                mockk<TradeSession> {
                    every { lastCandleClosePrice() } returns BigDecimal("100.0")
                    every { isMargin() } returns false
                    every { instrumentId } returns "12345"
                    every { strategyParameters } returns StrategyParameters(mapOf(LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 3))
                }

            //when
            val result = HardcodedOrderLotsQuantityStrategy(instrumentPersistencePort, operationServiceBrokerPort).getLotsQuantity(tradeSession)

            //then
            result.isRight().shouldBeTrue()
            result.getOrNull()!! shouldBe lotsQuantity
        }
    }

    "should get NotEnoughMoneyOnDepositError when there are not enough money on deposit" {
        //given
        val instrumentPersistencePort =
            mockk<InstrumentPersistencePort> {
                every { getByBrokerInstrumentId(any()) } returns
                        Instrument(
                            id = InstrumentId(UUID.randomUUID()),
                            name = "Сбербанк",
                            ticker = "SBER",
                            lot = 5,
                            brokerInstrumentId = "12345"
                        )
            }
        val operationServiceBrokerPort =
            mockk<OperationServiceBrokerPort> {
                every { getPortfolio() } returns Portfolio(BigDecimal("10.0"), BigDecimal.ZERO, BigDecimal("1000.0")).right()
            }
        val tradeSession =
            mockk<TradeSession> {
                every { lastCandleClosePrice() } returns BigDecimal("100.0")
                every { isMargin() } returns false
                every { instrumentId } returns "12345"
                every { strategyParameters } returns StrategyParameters(mapOf(LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 3))
            }

        //when
        val result = HardcodedOrderLotsQuantityStrategy(instrumentPersistencePort, operationServiceBrokerPort).getLotsQuantity(tradeSession)

        //then
        result.isLeft().shouldBeTrue()
        result.leftOrNull()!! shouldBe NotEnoughMoneyOnDepositError
    }

})