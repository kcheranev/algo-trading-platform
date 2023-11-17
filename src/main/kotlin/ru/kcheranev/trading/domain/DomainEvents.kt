package ru.kcheranev.trading.domain

import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.CandleInterval

sealed class DomainEvent

data class TradeSessionCreatedDomainEvent(
    val ticker: String,
    val candleInterval: CandleInterval
) : DomainEvent()

data class TradeSessionPendedForEntryDomainEvent(
    val id: TradeSessionId,
    val ticker: String,
    val candleInterval: CandleInterval
) : DomainEvent()

data class TradeSessionPendedForExitDomainEvent(
    val id: TradeSessionId,
    val ticker: String,
    val candleInterval: CandleInterval
) : DomainEvent()

data class TradeSessionEnteredDomainEvent(
    val id: TradeSessionId,
    val ticker: String,
    val candleInterval: CandleInterval
) : DomainEvent()

data class TradeSessionExitedDomainEvent(
    val id: TradeSessionId,
    val ticker: String,
    val candleInterval: CandleInterval
) : DomainEvent()