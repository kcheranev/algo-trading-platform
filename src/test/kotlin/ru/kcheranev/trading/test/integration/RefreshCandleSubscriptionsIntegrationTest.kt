package ru.kcheranev.trading.test.integration

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import ru.kcheranev.trading.core.port.income.subscription.RefreshCandleSubscriptionsUseCase
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.TradeStrategy
import ru.kcheranev.trading.domain.model.subscription.CandleSubscription
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import ru.kcheranev.trading.test.util.MarketDataSubscriptionInitializer
import java.util.UUID

@IntegrationTest
class RefreshCandleSubscriptionsIntegrationTest(
    private val refreshCandleSubscriptionsUseCase: RefreshCandleSubscriptionsUseCase,
    private val marketDataSubscriptionInitializer: MarketDataSubscriptionInitializer,
    private val candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder,
    private val tradeStrategyCache: TradeStrategyCache,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "refresh-candle-subscriptions"

    val marketDataBrokerGrpcStub = MarketDataBrokerGrpcStub(testName)

    "should delete unused subscription" {
        //given
        marketDataSubscriptionInitializer.addSubscription(
            Instrument("e6123145-9665-43e0-8413-cd61b8aa9b13", "SBER"),
            CandleInterval.ONE_MIN
        )
        marketDataSubscriptionInitializer.addSubscription(
            Instrument("b993e814-9986-4434-ae88-b086066714a0", "WUSH"),
            CandleInterval.ONE_MIN
        )
        val tradeStrategyId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeStrategyId,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b13",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                lotsQuantityInPosition = 10,
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1))
            )
        )
        val tradeStrategy =
            mockk<TradeStrategy> {
                every { isFreshCandleSeries(any(), any()) } returns true
            }
        tradeStrategyCache.put(tradeStrategyId, tradeStrategy)

        //when
        refreshCandleSubscriptionsUseCase.refreshCandleSubscriptions()

        //then
        val candleSubscriptions = candleSubscriptionCacheHolder.findAll()
        candleSubscriptions shouldHaveSize 1
        candleSubscriptions shouldContain
                CandleSubscription(
                    Instrument("e6123145-9665-43e0-8413-cd61b8aa9b13", "SBER"),
                    CandleInterval.ONE_MIN
                )

        marketDataBrokerGrpcStub.verifyForMarketDataStream("market-data-stream-unsubscribe.json")
    }

    "should add new subscription" {
        //given
        marketDataSubscriptionInitializer.addSubscription(
            Instrument("e6123145-9665-43e0-8413-cd61b8aa9b13", "SBER"),
            CandleInterval.ONE_MIN
        )
        val tradeStrategyId1 = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeStrategyId1,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b13",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                lotsQuantityInPosition = 10,
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1))
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
                lotsQuantity = 10,
                lotsQuantityInPosition = 10,
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1))
            )
        )
        val tradeStrategy =
            mockk<TradeStrategy> {
                every { isFreshCandleSeries(any(), any()) } returns true
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
                    CandleSubscription(
                        Instrument("e6123145-9665-43e0-8413-cd61b8aa9b13", "SBER"),
                        CandleInterval.ONE_MIN
                    ),
                    CandleSubscription(
                        Instrument("b993e814-9986-4434-ae88-b086066714a0", "WUSH"),
                        CandleInterval.ONE_MIN
                    )
                )

        marketDataBrokerGrpcStub.verifyForMarketDataStream("market-data-stream-subscribe.json")
    }

})