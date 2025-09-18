package com.github.trading.test.integration

import com.github.trading.common.date.DateSupplier
import com.github.trading.common.date.toMskZonedDateTime
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
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import com.github.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper
import com.github.trading.test.IntegrationTest
import com.github.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import com.github.trading.test.stub.http.TelegramNotificationHttpStub
import com.github.trading.test.util.MarketDataSubscriptionInitializer
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.ta4j.core.BaseBar
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
    private val marketDataSubscriptionInitializer: MarketDataSubscriptionInitializer,
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
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
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
                        BaseBar(
                            Duration.ofMinutes(1),
                            LocalDateTime.parse("2024-01-30T10:15:00").toMskZonedDateTime(),
                            BigDecimal(100),
                            BigDecimal(102),
                            BigDecimal(98),
                            BigDecimal(102),
                            BigDecimal(10)
                        )
                    )
                }
        val tradeStrategy =
            spyk(TradeStrategy(barSeries, false, mockk<Strategy>())) {
                every { shouldEnter(any()) } returns true
                every { shouldExit(any(Position::class)) } returns false
            }
        tradeStrategyCache.put(tradeSessionId, tradeStrategy)
        marketDataSubscriptionInitializer.addSubscription(
            Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
            CandleInterval.ONE_MIN
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
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )

        //when
        marketDataProcessingService.processIncomeCandle(ProcessIncomeCandleCommand(candle))

        //then
        val tradeSession = jdbcTemplate.findById(tradeSessionId, TradeSessionEntity::class.java)
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        marketDataBrokerGrpcStub.verifyForGetCandles("get-candles.json")
    }

})