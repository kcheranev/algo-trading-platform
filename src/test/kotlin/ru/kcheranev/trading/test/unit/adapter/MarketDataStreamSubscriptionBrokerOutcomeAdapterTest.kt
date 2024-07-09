package ru.kcheranev.trading.test.unit.adapter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import ru.kcheranev.trading.core.port.income.marketdata.ProcessCandleUseCase
import ru.kcheranev.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.subscription.CandleSubscription
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionHolder
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.MarketDataStreamSubscriptionBrokerOutcomeAdapter
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.subscribeCandlesWithWaitingClose
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
        val candleSubscriptionHolder = CandleSubscriptionHolder()
        val adapter =
            MarketDataStreamSubscriptionBrokerOutcomeAdapter(
                marketDataStreamService,
                processCandleUseCase,
                candleSubscriptionHolder
            )

        //when
        adapter.subscribeCandles(
            SubscribeCandlesOrderCommand(
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                CandleInterval.ONE_MIN
            )
        )

        //then
        val subscriptions = candleSubscriptionHolder.getSubscriptions()
        subscriptions shouldHaveSize 1
        subscriptions shouldContain
                CandleSubscription(
                    Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                    CandleInterval.ONE_MIN,
                    1
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
        val candleSubscriptionHolder = CandleSubscriptionHolder()
        candleSubscriptionHolder.incrementSubscriptionCount(
            Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
            CandleInterval.ONE_MIN
        )
        val adapter =
            MarketDataStreamSubscriptionBrokerOutcomeAdapter(
                marketDataStreamService,
                processCandleUseCase,
                candleSubscriptionHolder
            )

        //when
        adapter.subscribeCandles(
            SubscribeCandlesOrderCommand(
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                CandleInterval.ONE_MIN
            )
        )

        //then
        val subscriptions = candleSubscriptionHolder.getSubscriptions()
        subscriptions shouldHaveSize 1
        subscriptions shouldContain
                CandleSubscription(
                    Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                    CandleInterval.ONE_MIN,
                    2
                )

        verify(inverse = true) {
            marketDataSubscriptionService.subscribeCandlesWithWaitingClose(
                listOf("e6123145-9665-43e0-8413-cd61b8aa9b1"),
                SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
            )
        }
    }

    "should unsubscribe candles when there is one same subscription exists" {
        //given
        val marketDataSubscriptionService = mockk<MarketDataSubscriptionService>(relaxed = true)
        val marketDataStreamService =
            mockk<MarketDataStreamService> {
                every { getStreamById("candles_SBER_ONE_MIN") } returns marketDataSubscriptionService
            }
        val processCandleUseCase = mockk<ProcessCandleUseCase>()
        val candleSubscriptionHolder = CandleSubscriptionHolder()
        candleSubscriptionHolder.incrementSubscriptionCount(
            Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
            CandleInterval.ONE_MIN
        )
        val adapter =
            MarketDataStreamSubscriptionBrokerOutcomeAdapter(
                marketDataStreamService,
                processCandleUseCase,
                candleSubscriptionHolder
            )

        //when
        adapter.unsubscribeCandles(
            UnsubscribeCandlesOrderCommand(
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                CandleInterval.ONE_MIN
            )
        )

        //then
        val subscriptions = candleSubscriptionHolder.getSubscriptions()
        subscriptions shouldHaveSize 0

        verify {
            marketDataSubscriptionService.unsubscribeCandles(
                listOf("e6123145-9665-43e0-8413-cd61b8aa9b1"),
                SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
            )
        }
    }

    "should not unsubscribe candles when there are two same subscriptions exist" {
        //given
        val marketDataSubscriptionService = mockk<MarketDataSubscriptionService>(relaxed = true)
        val marketDataStreamService =
            mockk<MarketDataStreamService> {
                every { getStreamById("candles_SBER_ONE_MIN") } returns marketDataSubscriptionService
            }
        val processCandleUseCase = mockk<ProcessCandleUseCase>()
        val candleSubscriptionHolder = CandleSubscriptionHolder()
        val instrument = Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        candleSubscriptionHolder.incrementSubscriptionCount(instrument, CandleInterval.ONE_MIN)
        candleSubscriptionHolder.incrementSubscriptionCount(instrument, CandleInterval.ONE_MIN)
        val adapter =
            MarketDataStreamSubscriptionBrokerOutcomeAdapter(
                marketDataStreamService,
                processCandleUseCase,
                candleSubscriptionHolder
            )

        //when
        adapter.unsubscribeCandles(
            UnsubscribeCandlesOrderCommand(
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                CandleInterval.ONE_MIN
            )
        )

        //then
        val subscriptions = candleSubscriptionHolder.getSubscriptions()
        subscriptions shouldHaveSize 1
        subscriptions shouldContain
                CandleSubscription(
                    Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                    CandleInterval.ONE_MIN,
                    1
                )

        verify(inverse = true) {
            marketDataSubscriptionService.unsubscribeCandles(
                listOf("e6123145-9665-43e0-8413-cd61b8aa9b1"),
                SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
            )
        }
    }

})