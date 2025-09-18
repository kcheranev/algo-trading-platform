package com.github.trading.test.unit.adapter

import com.github.trading.core.port.income.marketdata.ProcessCandleUseCase
import com.github.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import com.github.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import com.github.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.subscription.CandleSubscription
import com.github.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
import com.github.trading.infra.adapter.outcome.broker.impl.MarketDataStreamSubscriptionBrokerOutcomeAdapter
import com.github.trading.infra.adapter.outcome.broker.impl.subscribeCandlesWithWaitingClose
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.tinkoff.piapi.core.stream.MarketDataStreamService
import ru.tinkoff.piapi.core.stream.MarketDataSubscriptionService

class MarketDataStreamSubscriptionBrokerOutcomeAdapterTest : StringSpec({

    "should subscribe candles" {
        //given
        mockkStatic(MarketDataSubscriptionService::subscribeCandlesWithWaitingClose)
        justRun { any<MarketDataSubscriptionService>().subscribeCandlesWithWaitingClose(any(), any()) }
        val marketDataSubscriptionService = mockk<MarketDataSubscriptionService>()
        val marketDataStreamService =
            mockk<MarketDataStreamService> {
                every { newStream("candles_SBER_ONE_MIN", any(), any()) } returns marketDataSubscriptionService
            }
        val processCandleUseCase = mockk<ProcessCandleUseCase>()
        val candleSubscriptionCacheHolder = CandleSubscriptionCacheHolder()
        val adapter =
            MarketDataStreamSubscriptionBrokerOutcomeAdapter(
                marketDataStreamService,
                processCandleUseCase,
                mockk<TradeSessionPersistencePort>(),
                candleSubscriptionCacheHolder
            )

        //when
        adapter.subscribeCandles(
            SubscribeCandlesOrderCommand(
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                CandleInterval.ONE_MIN
            )
        )

        //then
        val subscriptions = candleSubscriptionCacheHolder.findAll()
        subscriptions shouldHaveSize 1
        subscriptions shouldContain
                CandleSubscription(
                    Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                    CandleInterval.ONE_MIN,
                )

        verify {
            marketDataSubscriptionService.subscribeCandlesWithWaitingClose(
                listOf("e6123145-9665-43e0-8413-cd61b8aa9b1"),
                SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
            )
        }
    }

    "should subscribe candles when there is same subscription" {
        //given
        mockkStatic(MarketDataSubscriptionService::subscribeCandlesWithWaitingClose)
        justRun { any<MarketDataSubscriptionService>().subscribeCandlesWithWaitingClose(any(), any()) }
        val marketDataSubscriptionService = mockk<MarketDataSubscriptionService>()
        val marketDataStreamService =
            mockk<MarketDataStreamService> {
                every { newStream("candles_SBER_ONE_MIN", any(), any()) } returns marketDataSubscriptionService
            }
        val processCandleUseCase = mockk<ProcessCandleUseCase>()
        val candleSubscriptionCacheHolder = CandleSubscriptionCacheHolder()
        candleSubscriptionCacheHolder.add(
            CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"), CandleInterval.ONE_MIN)
        )
        val adapter =
            MarketDataStreamSubscriptionBrokerOutcomeAdapter(
                marketDataStreamService,
                processCandleUseCase,
                mockk<TradeSessionPersistencePort>(),
                candleSubscriptionCacheHolder
            )

        //when
        adapter.subscribeCandles(
            SubscribeCandlesOrderCommand(
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                CandleInterval.ONE_MIN
            )
        )

        //then
        val subscriptions = candleSubscriptionCacheHolder.findAll()
        subscriptions shouldHaveSize 1
        subscriptions shouldContain
                CandleSubscription(
                    Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                    CandleInterval.ONE_MIN
                )

        verify(inverse = true) {
            marketDataSubscriptionService.subscribeCandlesWithWaitingClose(
                listOf("e6123145-9665-43e0-8413-cd61b8aa9b1"),
                SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
            )
        }
    }

    "should unsubscribe candles when there is no same subscription exists" {
        //given
        val marketDataSubscriptionService = mockk<MarketDataSubscriptionService>(relaxed = true)
        val marketDataStreamService =
            mockk<MarketDataStreamService> {
                every { getStreamById("candles_SBER_ONE_MIN") } returns marketDataSubscriptionService
            }
        val processCandleUseCase = mockk<ProcessCandleUseCase>()
        val candleSubscriptionCacheHolder = CandleSubscriptionCacheHolder()
        candleSubscriptionCacheHolder.add(
            CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"), CandleInterval.ONE_MIN)
        )
        val tradeSessionPersistencePort =
            mockk<TradeSessionPersistencePort> {
                every { isReadyForOrderTradeSessionExists(any()) } returns false
            }
        val adapter =
            MarketDataStreamSubscriptionBrokerOutcomeAdapter(
                marketDataStreamService,
                processCandleUseCase,
                tradeSessionPersistencePort,
                candleSubscriptionCacheHolder
            )

        //when
        adapter.unsubscribeCandles(
            UnsubscribeCandlesOrderCommand(
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                CandleInterval.ONE_MIN
            )
        )

        //then
        val subscriptions = candleSubscriptionCacheHolder.findAll()
        subscriptions shouldHaveSize 0

        verify {
            marketDataSubscriptionService.unsubscribeCandles(
                listOf("e6123145-9665-43e0-8413-cd61b8aa9b1"),
                SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
            )
        }
    }

    "should not unsubscribe candles when there is same subscriptions exist" {
        //given
        val marketDataSubscriptionService = mockk<MarketDataSubscriptionService>(relaxed = true)
        val marketDataStreamService =
            mockk<MarketDataStreamService> {
                every { getStreamById("candles_SBER_ONE_MIN") } returns marketDataSubscriptionService
            }
        val processCandleUseCase = mockk<ProcessCandleUseCase>()
        val candleSubscriptionCacheHolder = CandleSubscriptionCacheHolder()
        candleSubscriptionCacheHolder.add(
            CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"), CandleInterval.ONE_MIN)
        )
        val tradeSessionPersistencePort =
            mockk<TradeSessionPersistencePort> {
                every { isReadyForOrderTradeSessionExists(any()) } returns true
            }
        val adapter =
            MarketDataStreamSubscriptionBrokerOutcomeAdapter(
                marketDataStreamService,
                processCandleUseCase,
                tradeSessionPersistencePort,
                candleSubscriptionCacheHolder
            )

        //when
        adapter.unsubscribeCandles(
            UnsubscribeCandlesOrderCommand(
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                CandleInterval.ONE_MIN
            )
        )

        //then
        val subscriptions = candleSubscriptionCacheHolder.findAll()
        subscriptions shouldHaveSize 1
        subscriptions shouldContain
                CandleSubscription(
                    Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                    CandleInterval.ONE_MIN
                )

        verify(inverse = true) {
            marketDataSubscriptionService.unsubscribeCandles(
                listOf("e6123145-9665-43e0-8413-cd61b8aa9b1"),
                SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
            )
        }
    }

})