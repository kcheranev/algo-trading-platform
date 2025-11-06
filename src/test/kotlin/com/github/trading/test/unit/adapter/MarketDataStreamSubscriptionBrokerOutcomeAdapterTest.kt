package com.github.trading.test.unit.adapter

import com.github.trading.core.port.income.marketdata.ProcessCandleUseCase
import com.github.trading.core.port.outcome.broker.SubscribeCandlesCommand
import com.github.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import com.github.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.subscription.CandleSubscription
import com.github.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
import com.github.trading.infra.adapter.outcome.broker.impl.MarketDataStreamSubscriptionBrokerOutcomeAdapter
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.ttech.piapi.core.impl.marketdata.MarketDataStreamManager
import ru.ttech.piapi.core.impl.marketdata.subscription.CandleSubscriptionSpec

class MarketDataStreamSubscriptionBrokerOutcomeAdapterTest : StringSpec({

    "should subscribe candles" {
        //given
        val marketDataStreamManager = mockk<MarketDataStreamManager>(relaxed = true)
        val candleSubscriptionCacheHolder = CandleSubscriptionCacheHolder()
        val adapter =
            MarketDataStreamSubscriptionBrokerOutcomeAdapter(
                marketDataStreamManager,
                mockk<ProcessCandleUseCase>(),
                mockk<TradeSessionPersistencePort>(),
                candleSubscriptionCacheHolder
            )

        //when
        adapter.subscribeCandles(
            SubscribeCandlesCommand(
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                CandleInterval.ONE_MIN
            )
        )

        //then
        val subscriptions = candleSubscriptionCacheHolder.findAll()
        subscriptions shouldHaveSize 1
        subscriptions shouldContain CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"), CandleInterval.ONE_MIN)

        verify {
            marketDataStreamManager.subscribeCandles(
                setOf(ru.ttech.piapi.core.impl.marketdata.subscription.Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE)),
                any<CandleSubscriptionSpec>(),
                any()
            )
        }
    }

    "should not subscribe candles when there is same subscription" {
        //given
        val marketDataStreamManager = mockk<MarketDataStreamManager>(relaxed = true)
        val candleSubscriptionCacheHolder = CandleSubscriptionCacheHolder()
        candleSubscriptionCacheHolder.add(
            CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"), CandleInterval.ONE_MIN)
        )
        val adapter =
            MarketDataStreamSubscriptionBrokerOutcomeAdapter(
                marketDataStreamManager,
                mockk<ProcessCandleUseCase>(),
                mockk<TradeSessionPersistencePort>(),
                candleSubscriptionCacheHolder
            )

        //when
        adapter.subscribeCandles(
            SubscribeCandlesCommand(
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                CandleInterval.ONE_MIN
            )
        )

        //then
        val subscriptions = candleSubscriptionCacheHolder.findAll()
        subscriptions shouldHaveSize 1
        subscriptions shouldContain CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"), CandleInterval.ONE_MIN)

        verify(inverse = true) {
            marketDataStreamManager.subscribeCandles(
                setOf(ru.ttech.piapi.core.impl.marketdata.subscription.Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE)),
                any<CandleSubscriptionSpec>(),
                any()
            )
        }
    }

    "should unsubscribe candles when there is no same subscription exists" {
        //given
        val marketDataStreamManager = mockk<MarketDataStreamManager>(relaxed = true)
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
                marketDataStreamManager,
                mockk<ProcessCandleUseCase>(),
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
            marketDataStreamManager.unsubscribeCandles(
                setOf(ru.ttech.piapi.core.impl.marketdata.subscription.Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE)),
                any<CandleSubscriptionSpec>()
            )
        }
    }

    "should not unsubscribe candles when there is same subscriptions exist" {
        //given
        val marketDataStreamManager = mockk<MarketDataStreamManager>(relaxed = true)
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
                marketDataStreamManager,
                mockk<ProcessCandleUseCase>(),
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
        subscriptions shouldContain CandleSubscription(Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"), CandleInterval.ONE_MIN)

        verify(inverse = true) {
            marketDataStreamManager.unsubscribeCandles(
                setOf(ru.ttech.piapi.core.impl.marketdata.subscription.Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE)),
                any<CandleSubscriptionSpec>()
            )
        }
    }

})