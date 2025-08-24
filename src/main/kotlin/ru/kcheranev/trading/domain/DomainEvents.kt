package ru.kcheranev.trading.domain

import ru.kcheranev.trading.domain.entity.TradeSession

sealed class DomainEvent

sealed class TradeSessionDomainEvent : DomainEvent()

data class TradeSessionCreatedDomainEvent(
    val tradeSession: TradeSession
) : TradeSessionDomainEvent()

data class TradeSessionPendedForEntryDomainEvent(
    val tradeSession: TradeSession
) : TradeSessionDomainEvent()

data class TradeSessionPendedForExitDomainEvent(
    val tradeSession: TradeSession
) : TradeSessionDomainEvent()

data class TradeSessionEnteredDomainEvent(
    val tradeSession: TradeSession,
    val lotsRequested: Int
) : TradeSessionDomainEvent()

data class TradeSessionExitedDomainEvent(
    val tradeSession: TradeSession,
    val lotsRequested: Int,
    val lotsExecuted: Int
) : TradeSessionDomainEvent()

data class TradeSessionStoppedDomainEvent(
    val tradeSession: TradeSession
) : TradeSessionDomainEvent()

data class TradeStrategySeriesCandleAddedDomainEvent(
    val tradeSession: TradeSession
) : TradeSessionDomainEvent()

data class TradeSessionResumedDomainEvent(
    val tradeSession: TradeSession
) : TradeSessionDomainEvent()