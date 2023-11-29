package ru.kcheranev.trading.domain.entity

import org.springframework.data.domain.AbstractAggregateRoot
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeriesBuilder
import ru.kcheranev.trading.common.LoggerDelegate
import ru.kcheranev.trading.core.strategy.StrategyFactory
import ru.kcheranev.trading.domain.TradeSessionCreatedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionEnteredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExitedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionNotExistsException
import ru.kcheranev.trading.domain.TradeSessionPendedForEntryDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForExitDomainEvent
import ru.kcheranev.trading.domain.UnexpectedTradeSessionTransitionException
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.IN_POSITION
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.PENDING_ENTER
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.PENDING_EXIT
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.WAITING
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.TradeStrategy
import java.time.LocalDateTime
import java.util.function.Supplier

data class TradeSession(
    val id: TradeSessionId?,
    val ticker: String,
    val instrumentId: String,
    var status: TradeSessionStatus,
    val startDate: LocalDateTime,
    val candleInterval: CandleInterval,
    var lastEventDate: LocalDateTime?,
    val strategy: TradeStrategy,
    val strategyConfigurationId: StrategyConfigurationId
) : AbstractAggregateRoot<TradeSession>() {

    fun addBar(candle: Candle) {
        strategy.addBar(domainModelMapper.map(candle))
        lastEventDate = candle.endTime
    }

    fun shouldEnter() = status.transitionAvailable(PENDING_ENTER) && strategy.shouldEnter(strategy.series.endIndex)

    fun shouldExit() = status.transitionAvailable(PENDING_EXIT) && strategy.shouldExit(strategy.series.endIndex)

    fun pendingEnter() {
        checkTransition(PENDING_ENTER)
        status = PENDING_ENTER
        registerEvent(TradeSessionPendedForEntryDomainEvent(id!!, ticker, candleInterval))
        logger.info("Trade session ${id.value} is pended for entry")
    }

    fun enter() {
        checkTransition(IN_POSITION)
        status = IN_POSITION
        registerEvent(TradeSessionEnteredDomainEvent(id!!, ticker, candleInterval))
        logger.info("Trade session ${id.value} has been entered")
    }

    fun pendingExit() {
        checkTransition(PENDING_EXIT)
        status = PENDING_EXIT
        registerEvent(TradeSessionPendedForExitDomainEvent(id!!, ticker, candleInterval))
        logger.info("Trade session ${id.value} is pended for exit")
    }

    fun exit() {
        checkTransition(WAITING)
        status = WAITING
        registerEvent(TradeSessionExitedDomainEvent(id!!, ticker, candleInterval))
        logger.info("Trade session ${id.value} has been exited")
    }

    private fun checkTransition(toStatus: TradeSessionStatus) {
        if (id == null) {
            throw TradeSessionNotExistsException()
        }
        if (!status.transitionAvailable(toStatus)) {
            throw UnexpectedTradeSessionTransitionException(id, status, IN_POSITION)
        }
    }

    companion object {

        private val logger by LoggerDelegate()

        fun start(
            strategyConfiguration: StrategyConfiguration,
            ticker: String,
            instrumentId: String,
            candles: List<Candle>,
            strategyFactory: StrategyFactory
        ): TradeSession {
            val candleInterval = strategyConfiguration.candleInterval
            val series: BarSeries = BaseBarSeriesBuilder()
                .withName("Trade session: ticker=$ticker, candleInterval=$candleInterval")
                .build()
            candles.forEach { series.addBar(domainModelMapper.map(it)) }
            val tradeSession = TradeSession(
                id = null,
                ticker = ticker,
                instrumentId = instrumentId,
                status = WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = candleInterval,
                lastEventDate = candles.last().endTime,
                strategy = strategyFactory.initStrategy(strategyConfiguration.params, series),
                strategyConfigurationId = strategyConfiguration.id
            )
            tradeSession.registerEvent(TradeSessionCreatedDomainEvent(ticker, candleInterval))
            logger.info("Trade session: ticker=$ticker, candleInterval=$candleInterval is starting...")
            return tradeSession
        }

    }

}

data class TradeSessionId(
    val value: Long
)

enum class TradeSessionStatus(
    private val availableTransitions: Supplier<Set<TradeSessionStatus>>
) {

    WAITING({ setOf(PENDING_ENTER, FAILED, STOPPED) }),
    PENDING_ENTER({ setOf(IN_POSITION, FAILED, STOPPED) }),
    IN_POSITION({ setOf(PENDING_EXIT, FAILED, STOPPED) }),
    PENDING_EXIT({ setOf(WAITING, FAILED, STOPPED) }),
    FAILED({ emptySet() }),
    STOPPED({ emptySet() });

    fun transitionAvailable(transition: TradeSessionStatus) = transition in availableTransitions.get()

}