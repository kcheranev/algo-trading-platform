package com.github.trading.test.integration

import com.github.trading.common.date.DateSupplier
import com.github.trading.common.date.toMskInstant
import com.github.trading.core.port.income.marketdata.ProcessIncomeCandleCommand
import com.github.trading.core.service.MarketDataProcessingService
import com.github.trading.core.strategy.lotsquantity.LOTS_QUANTITY_STRATEGY_PARAMETER_NAME
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.Candle
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.Position
import com.github.trading.domain.model.TradeStrategy
import com.github.trading.domain.model.subscription.CandleSubscription
import com.github.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import com.github.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper
import com.github.trading.test.IntegrationTest
import com.github.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import com.github.trading.test.stub.http.TelegramNotificationHttpStub
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.ta4j.core.BaseBarSeriesBuilder
import org.ta4j.core.Strategy
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@IntegrationTest
class ReinitializeStrategyAfterDelayIntegrationTest(
    private val marketDataProcessingService: MarketDataProcessingService,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val tradeStrategyCache: TradeStrategyCache,
    private val candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder,
    private val telegramNotificationHttpStub: TelegramNotificationHttpStub,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "reinitialize-strategy-after-delay"

    val marketDataBrokerGrpcStub = MarketDataBrokerGrpcStub(testName)

    "should reinitialize strategy after delay" {
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
        val tradeStrategy =
            spyk(TradeStrategy(barSeries, false, mockk<Strategy>())) {
                every { shouldEnter(any()) } returns true
                every { shouldExit(any(Position::class)) } returns false
            }
        tradeStrategyCache.put(tradeSessionId, tradeStrategy)
        candleSubscriptionCacheHolder.add(
            CandleSubscription(Instrument("926fdfbf-4b07-47c9-8928-f49858ca33f2", "ABRD"), CandleInterval.ONE_MIN)
        )
        every { DateSupplier.currentDateTime() } returns LocalDateTime.parse("2024-01-30T10:22:00")
        marketDataBrokerGrpcStub.stubForGetCandles("get-candles.json")
        telegramNotificationHttpStub.stubForSendNotification()
        val candle =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(102),
                closePrice = BigDecimal(104),
                highestPrice = BigDecimal(104),
                lowestPrice = BigDecimal(97),
                volume = 10,
                endDateTime = LocalDateTime.parse("2024-01-30T10:21:00"),
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2"
            )

        //when
        marketDataProcessingService.processIncomeCandle(ProcessIncomeCandleCommand(candle))

        //then
        val tradeSession = jdbcTemplate.findById(tradeSessionId, TradeSessionEntity::class.java)
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        marketDataBrokerGrpcStub.verifyForGetCandles("get-candles.json")
    }

})