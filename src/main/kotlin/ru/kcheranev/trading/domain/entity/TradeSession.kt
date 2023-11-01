package ru.kcheranev.trading.domain.entity

import ru.kcheranev.trading.common.LoggerDelegate
import ru.kcheranev.trading.core.strategy.TradeStrategy
import java.time.LocalDate

data class TradeSession(
    val id: TradeSessionId?,
    val ticker: String,
    var status: TradeSessionStatus,
    val startDate: LocalDate,
    var lastEventDate: LocalDate?,
    val strategy: TradeStrategy
) {

    companion object {

        val logger by LoggerDelegate()

        fun create(
            ticker: String,
            strategy: TradeStrategy
        ): TradeSession =
            TradeSession(
                id = null,
                ticker = ticker,
                status = TradeSessionStatus.PENDING_ENTER,
                startDate = LocalDate.now(),
                lastEventDate = null,
                strategy = strategy
            )

    }

}

data class TradeSessionId(
    val value: Int
)

enum class TradeSessionStatus(
    private val availableTransitions: Set<TradeSessionStatus>
) {

    PENDING_ENTER(emptySet()),
    PENDING_EXIT(setOf(PENDING_ENTER)),
    FAILED(setOf(PENDING_ENTER, PENDING_EXIT)),
    STOPPED(setOf(PENDING_ENTER, PENDING_EXIT));

    fun transitionAvailable(transition: TradeSessionStatus) = transition in availableTransitions

}