package ru.kcheranev.trading.domain

import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument

sealed class DomainEvent

data class TradeSessionCreatedDomainEvent(
    val instrument: Instrument,
    val candleInterval: CandleInterval
) : DomainEvent()

data class TradeSessionPendedForEntryDomainEvent(
    val tradeSessionId: TradeSessionId,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val lotsQuantity: Int
) : DomainEvent()

data class TradeSessionPendedForExitDomainEvent(
    val tradeSessionId: TradeSessionId,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val lotsQuantity: Int
) : DomainEvent()

data class TradeSessionEnteredDomainEvent(
    val tradeSessionId: TradeSessionId,
    val instrument: Instrument,
    val candleInterval: CandleInterval
) : DomainEvent()

data class TradeSessionExitedDomainEvent(
    val tradeSessionId: TradeSessionId,
    val instrument: Instrument,
    val candleInterval: CandleInterval
) : DomainEvent()

data class TradeSessionStoppedDomainEvent(
    val tradeSessionId: TradeSessionId,
    val instrument: Instrument,
    val candleInterval: CandleInterval
) : DomainEvent()

data class TradeSessionExpiredDomainEvent(
    val tradeSessionId: TradeSessionId,
    val instrument: Instrument,
    val candleInterval: CandleInterval
) : DomainEvent()

data class TradeStrategySeriesCandleAddedDomainEvent(
    val tradeSessionId: TradeSessionId,
    val candle: Candle,
    val instrument: Instrument
) : DomainEvent()