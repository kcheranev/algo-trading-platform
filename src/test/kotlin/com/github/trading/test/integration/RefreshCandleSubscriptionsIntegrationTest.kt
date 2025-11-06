package com.github.trading.test.integration

import com.github.trading.core.port.income.subscription.RefreshCandleSubscriptionsUseCase
import com.github.trading.core.strategy.lotsquantity.LOTS_QUANTITY_STRATEGY_PARAMETER_NAME
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.TradeStrategy
import com.github.trading.domain.model.subscription.CandleSubscription
import com.github.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import com.github.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper
import com.github.trading.test.IntegrationTest
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.ttech.piapi.core.impl.marketdata.MarketDataStreamManager
import ru.ttech.piapi.core.impl.marketdata.subscription.CandleSubscriptionSpec
import java.math.BigDecimal
import java.util.UUID

@IntegrationTest
class RefreshCandleSubscriptionsIntegrationTest(
    private val refreshCandleSubscriptionsUseCase: RefreshCandleSubscriptionsUseCase,
    private val candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder,
    private val tradeStrategyCache: TradeStrategyCache,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val marketDataStreamManager: MarketDataStreamManager,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "refresh-candle-subscriptions"

    "should delete unused subscription" {
        //given
        candleSubscriptionCacheHolder.add(
            CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b13", "SBER"), CandleInterval.ONE_MIN)
        )
        candleSubscriptionCacheHolder.add(
            CandleSubscription(Instrument("b993e814-9986-4434-ae88-b086066714a0", "WUSH"), CandleInterval.ONE_MIN)
        )
        val tradeStrategyId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeStrategyId,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b13",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.HARDCODED,
                positionLotsQuantity = 10,
                positionAveragePrice = BigDecimal("100"),
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        )
        val tradeStrategy =
            mockk<TradeStrategy> {
                every { isFreshCandleSeries(any()) } returns true
            }
        tradeStrategyCache.put(tradeStrategyId, tradeStrategy)

        //when
        refreshCandleSubscriptionsUseCase.refreshCandleSubscriptions()

        //then
        val candleSubscriptions = candleSubscriptionCacheHolder.findAll()
        candleSubscriptions shouldHaveSize 1
        candleSubscriptions shouldContain CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b13", "SBER"), CandleInterval.ONE_MIN)

        verify {
            marketDataStreamManager.unsubscribeCandles(
                setOf(ru.ttech.piapi.core.impl.marketdata.subscription.Instrument("b993e814-9986-4434-ae88-b086066714a0", SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE)),
                any<CandleSubscriptionSpec>()
            )
        }
    }

    "should add new subscription" {
        //given
        candleSubscriptionCacheHolder.add(
            CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b13", "SBER"), CandleInterval.ONE_MIN)
        )
        val tradeStrategyId1 = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeStrategyId1,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b13",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.HARDCODED,
                positionLotsQuantity = 10,
                positionAveragePrice = BigDecimal("100"),
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        )
        val tradeStrategyId2 = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeStrategyId2,
                ticker = "WUSH",
                instrumentId = "b993e814-9986-4434-ae88-b086066714a0",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.HARDCODED,
                positionLotsQuantity = 5,
                positionAveragePrice = BigDecimal("50"),
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        )
        val tradeStrategy =
            mockk<TradeStrategy> {
                every { isFreshCandleSeries(any()) } returns true
            }
        tradeStrategyCache.put(tradeStrategyId1, tradeStrategy)
        tradeStrategyCache.put(tradeStrategyId2, tradeStrategy)

        //when
        refreshCandleSubscriptionsUseCase.refreshCandleSubscriptions()

        //then
        val candleSubscriptions = candleSubscriptionCacheHolder.findAll()
        candleSubscriptions shouldHaveSize 2
        candleSubscriptions shouldContainExactlyInAnyOrder
                listOf(
                    CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b13", "SBER"), CandleInterval.ONE_MIN),
                    CandleSubscription(Instrument("b993e814-9986-4434-ae88-b086066714a0", "WUSH"), CandleInterval.ONE_MIN)
                )

        verify {
            marketDataStreamManager.subscribeCandles(
                setOf(ru.ttech.piapi.core.impl.marketdata.subscription.Instrument("b993e814-9986-4434-ae88-b086066714a0", SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE)),
                any<CandleSubscriptionSpec>(),
                any()
            )
        }
    }

})