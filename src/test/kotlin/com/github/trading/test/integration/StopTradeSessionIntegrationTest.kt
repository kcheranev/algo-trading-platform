package com.github.trading.test.integration

import com.github.trading.common.date.toMskInstant
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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.ta4j.core.BaseBarSeriesBuilder
import org.ta4j.core.Strategy
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.ttech.piapi.core.impl.marketdata.MarketDataStreamManager
import ru.ttech.piapi.core.impl.marketdata.subscription.CandleSubscriptionSpec
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@IntegrationTest
class StopTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val tradeStrategyCache: TradeStrategyCache,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder,
    private val tradeSessionCache: TradeStrategyCache,
    private val marketDataStreamManager: MarketDataStreamManager,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "stop-trade-session"

    "should stop trade session" {
        //given
        val tradeSessionId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSessionId,
                ticker = "ABRD",
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                status = TradeSessionStatus.WAITING,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.HARDCODED,
                positionLotsQuantity = 0,
                positionAveragePrice = BigDecimal.ZERO,
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        )
        val barSeries =
            BaseBarSeriesBuilder().build()
                .apply {
                    addBar(
                        barBuilder()
                            .timePeriod(Duration.ofMinutes(1))
                            .endTime(LocalDateTime.parse("2024-01-30T10:15:00").toMskInstant())
                            .openPrice(BigDecimal(100))
                            .highPrice(BigDecimal(102))
                            .lowPrice(BigDecimal(98))
                            .closePrice(BigDecimal(102))
                            .volume(10)
                            .build()
                    )
                }
        tradeStrategyCache.put(tradeSessionId, TradeStrategy(barSeries, false, mockk<Strategy>()))
        candleSubscriptionCacheHolder.add(
            CandleSubscription(Instrument("926fdfbf-4b07-47c9-8928-f49858ca33f2", "ABRD"), CandleInterval.ONE_MIN)
        )

        //when
        val response = testRestTemplate.exchange(
            "/trade-sessions/${tradeSessionId}/stop",
            HttpMethod.POST,
            HttpEntity.EMPTY,
            Unit::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK

        verify {
            marketDataStreamManager.unsubscribeCandles(
                setOf(ru.ttech.piapi.core.impl.marketdata.subscription.Instrument("926fdfbf-4b07-47c9-8928-f49858ca33f2", SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE)),
                any<CandleSubscriptionSpec>()
            )
        }

        val tradeSessionList = jdbcTemplate.findAll(TradeSessionEntity::class.java)
        tradeSessionList.shouldHaveSize(1)
        val tradeSession = tradeSessionList.first()
        tradeSession.status shouldBe TradeSessionStatus.STOPPED

        tradeSessionCache.tradeStrategies.shouldBeEmpty()
        candleSubscriptionCacheHolder.findAll().shouldBeEmpty()
    }

})