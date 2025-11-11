package com.github.trading.test.unit.lotsquantity

import arrow.core.right
import com.github.trading.core.error.DomainError
import com.github.trading.core.port.outcome.broker.OperationServiceBrokerPort
import com.github.trading.core.port.outcome.broker.OrderServiceBrokerPort
import com.github.trading.core.port.outcome.broker.model.GetMaxLotsResponse
import com.github.trading.core.port.outcome.persistence.instrument.InstrumentPersistencePort
import com.github.trading.core.strategy.lotsquantity.DEPOSIT_PERCENT_STRATEGY_PARAMETER_NAME
import com.github.trading.core.strategy.lotsquantity.DepositPercentOrderLotsQuantityStrategy
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

class DepositPercentOrderLotsQuantityStrategyTest : FreeSpec({

    "should get lots quantity" - {
        data class TestParameters(
            val totalPortfolioAmount: BigDecimal,
            val buyMaxMarketMarginLots: Int,
            val lotsQuantity: Int
        )
        withData(
            nameFn = { "totalPortfolioAmount = ${it.totalPortfolioAmount}, buyMaxMarketMarginLots = ${it.buyMaxMarketMarginLots}, lotsQuantity = ${it.lotsQuantity}" },
            TestParameters(BigDecimal("5000.0"), 5, 3),
            TestParameters(BigDecimal("5000.0"), 2, 2)
        ) { (totalPortfolioAmount, buyMaxMarketMarginLots, lotsQuantity) ->
            //given
            val instrumentPersistencePort =
                mockk<InstrumentPersistencePort> {
                    every { getByBrokerInstrumentId(any()) } returns
                            Instrument(
                                id = InstrumentId(UUID.randomUUID()),
                                name = "АбрауДюрсо",
                                ticker = "ABRD",
                                lot = 5,
                                brokerInstrumentId = "12345"
                            )
                }
            val operationServiceBrokerPort =
                mockk<OperationServiceBrokerPort> {
                    every { getPortfolio() } returns Portfolio(BigDecimal("1500"), BigDecimal.ZERO, totalPortfolioAmount).right()
                }
            val orderServiceBrokerPort =
                mockk<OrderServiceBrokerPort> {
                    every { getMaxLots(any()) } returns
                            mockk<GetMaxLotsResponse> {
                                every { buyMarginLimits.buyMaxMarketLots } returns buyMaxMarketMarginLots
                            }.right()
                }
            val tradeSession =
                mockk<TradeSession> {
                    every { lastCandleClosePrice() } returns BigDecimal("100.0")
                    every { isMargin() } returns false
                    every { instrumentId } returns "12345"
                    every { instrument } returns com.github.trading.domain.model.Instrument("12345", "ANY")
                    every { strategyParameters } returns StrategyParameters(mapOf(DEPOSIT_PERCENT_STRATEGY_PARAMETER_NAME to BigDecimal("0.33")))
                }

            //when
            val result = DepositPercentOrderLotsQuantityStrategy(orderServiceBrokerPort, instrumentPersistencePort, operationServiceBrokerPort).getLotsQuantity(tradeSession)

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
                            name = "АбрауДюрсо",
                            ticker = "ABRD",
                            lot = 5,
                            brokerInstrumentId = "12345"
                        )
            }
        val operationServiceBrokerPort =
            mockk<OperationServiceBrokerPort> {
                every { getPortfolio() } returns Portfolio(BigDecimal("10.0"), BigDecimal.ZERO, BigDecimal("1000.0")).right()
            }
        val orderServiceBrokerPort =
            mockk<OrderServiceBrokerPort> {
                every { getMaxLots(any()) } returns
                        mockk<GetMaxLotsResponse> {
                            every { buyMarginLimits.buyMaxMarketLots } returns 0
                        }.right()
            }
        val tradeSession =
            mockk<TradeSession> {
                every { lastCandleClosePrice() } returns BigDecimal("100.0")
                every { isMargin() } returns false
                every { instrumentId } returns "12345"
                every { instrument } returns com.github.trading.domain.model.Instrument("12345", "ANY")
                every { strategyParameters } returns StrategyParameters(mapOf(DEPOSIT_PERCENT_STRATEGY_PARAMETER_NAME to BigDecimal("0.33")))
            }

        //when
        val result = DepositPercentOrderLotsQuantityStrategy(orderServiceBrokerPort, instrumentPersistencePort, operationServiceBrokerPort).getLotsQuantity(tradeSession)

        //then
        result.isLeft().shouldBeTrue()
        result.leftOrNull()!! shouldBe DomainError.NotEnoughMoneyOnDepositError
    }

})