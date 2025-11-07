package com.github.trading.test.unit.lotsquantity

import arrow.core.right
import com.github.trading.core.error.DomainError
import com.github.trading.core.port.outcome.broker.OrderServiceBrokerPort
import com.github.trading.core.port.outcome.broker.model.GetMaxLotsResponse
import com.github.trading.core.strategy.lotsquantity.HardcodedOrderLotsQuantityStrategy
import com.github.trading.core.strategy.lotsquantity.LOTS_QUANTITY_STRATEGY_PARAMETER_NAME
import com.github.trading.domain.entity.TradeSession
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.StrategyParameters
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class HardcodedOrderLotsQuantityStrategyTest : FreeSpec({

    "should get lots quantity" - {
        data class TestParameters(
            val buyMaxMarketMarginLots: Int,
            val lotsQuantity: Int
        )
        withData(
            nameFn = { "buyMaxMarketMarginLots = ${it.buyMaxMarketMarginLots}, lotsQuantity = ${it.lotsQuantity}" },
            TestParameters(5, 3),
            TestParameters(2, 2)
        ) { (buyMaxMarketMarginLots, lotsQuantity) ->
            //given
            val orderServiceBrokerPort =
                mockk<OrderServiceBrokerPort> {
                    every { getMaxLots(any()) } returns
                            mockk<GetMaxLotsResponse> {
                                every { buyMarginLimits.buyMaxMarketLots } returns buyMaxMarketMarginLots
                            }.right()
                }
            val tradeSession =
                mockk<TradeSession> {
                    every { isMargin() } returns false
                    every { instrument } returns Instrument("12345", "ANY")
                    every { strategyParameters } returns StrategyParameters(mapOf(LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 3))
                }

            //when
            val result = HardcodedOrderLotsQuantityStrategy(orderServiceBrokerPort).getLotsQuantity(tradeSession)

            //then
            result.isRight().shouldBeTrue()
            result.getOrNull()!! shouldBe lotsQuantity
        }
    }

    "should get NotEnoughMoneyOnDepositError when there are not enough money on deposit" {
        //given
        val orderServiceBrokerPort =
            mockk<OrderServiceBrokerPort> {
                every { getMaxLots(any()) } returns
                        mockk<GetMaxLotsResponse> {
                            every { buyMarginLimits.buyMaxMarketLots } returns 0
                        }.right()
            }
        val tradeSession =
            mockk<TradeSession> {
                every { isMargin() } returns false
                every { instrument } returns Instrument("12345", "ANY")
                every { strategyParameters } returns StrategyParameters(mapOf(LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 3))
            }

        //when
        val result = HardcodedOrderLotsQuantityStrategy(orderServiceBrokerPort).getLotsQuantity(tradeSession)

        //then
        result.isLeft().shouldBeTrue()
        result.leftOrNull()!! shouldBe DomainError.NotEnoughMoneyOnDepositError
    }

})