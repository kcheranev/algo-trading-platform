package ru.kcheranev.trading.domain.entity

import org.slf4j.LoggerFactory
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeriesBuilder
import ru.kcheranev.trading.common.DateSupplier
import ru.kcheranev.trading.core.strategy.StrategyFactory
import ru.kcheranev.trading.domain.TradeSessionCreatedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionEnteredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExitedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionNotExistsException
import ru.kcheranev.trading.domain.TradeSessionPendedForEntryDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForExitDomainEvent
import ru.kcheranev.trading.domain.TradeSessionStoppedDomainEvent
import ru.kcheranev.trading.domain.UnexpectedTradeSessionTransitionException
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.IN_POSITION
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.PENDING_ENTER
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.PENDING_EXIT
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.STOPPED
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.WAITING
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
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
    val lotsQuantity: Int,
    var lastEventDate: LocalDateTime,
    val strategy: TradeStrategy,
    val strategyConfigurationId: StrategyConfigurationId
) : AbstractAggregateRoot() {

    private val log = LoggerFactory.getLogger(javaClass)

    fun addBar(candle: Candle) {
        strategy.addBar(domainModelMapper.map(candle))
        lastEventDate = candle.endTime
    }

    fun shouldEnter() = status.transitionAvailable(PENDING_ENTER) && strategy.shouldEnter(strategy.series.endIndex)

    fun shouldExit() = status.transitionAvailable(PENDING_EXIT) && strategy.shouldExit(strategy.series.endIndex)

    fun pendingEnter() {
        checkTransition(PENDING_ENTER)
        status = PENDING_ENTER
        registerEvent(
            TradeSessionPendedForEntryDomainEvent(id!!, Instrument(instrumentId, ticker), candleInterval, lotsQuantity)
        )
        log.info("Trade session ${id.value} is pended for entry")
    }

    fun enter() {
        checkTransition(IN_POSITION)
        status = IN_POSITION
        registerEvent(TradeSessionEnteredDomainEvent(id!!, Instrument(instrumentId, ticker), candleInterval))
        log.info("Trade session ${id.value} has been entered")
    }

    fun pendingExit() {
        checkTransition(PENDING_EXIT)
        status = PENDING_EXIT
        registerEvent(
            TradeSessionPendedForExitDomainEvent(
                id!!,
                Instrument(instrumentId, ticker),
                candleInterval,
                lotsQuantity
            )
        )
        log.info("Trade session ${id.value} is pended for exit")
    }

    fun exit() {
        checkTransition(WAITING)
        status = WAITING
        registerEvent(TradeSessionExitedDomainEvent(id!!, Instrument(instrumentId, ticker), candleInterval))
        log.info("Trade session ${id.value} has been exited")
    }

    fun stop() {
        checkTransition(STOPPED)
        status = STOPPED
        registerEvent(TradeSessionStoppedDomainEvent(id!!, Instrument(instrumentId, ticker), candleInterval))
        log.info("Trade session ${id.value} has been stopped")
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

        fun start(
            strategyConfiguration: StrategyConfiguration,
            ticker: String,
            instrumentId: String,
            lotsQuantity: Int,
            candles: List<Candle>,
            strategyFactory: StrategyFactory,
            dateSupplier: DateSupplier
        ): TradeSession {
            val candleInterval = strategyConfiguration.candleInterval
            val series: BarSeries = BaseBarSeriesBuilder()
                .withName("Trade session: ticker=$ticker, candleInterval=$candleInterval")
                .build()
            candles.forEach { series.addBar(domainModelMapper.map(it)) }
            val tradeSession =
                TradeSession(
                    id = null,
                    ticker = ticker,
                    instrumentId = instrumentId,
                    status = WAITING,
                    startDate = dateSupplier.currentDate(),
                    candleInterval = candleInterval,
                    lotsQuantity = lotsQuantity,
                    lastEventDate = candles.last().endTime,
                    strategy = strategyFactory.initStrategy(strategyConfiguration.params, series),
                    strategyConfigurationId = strategyConfiguration.id!!
                )
            tradeSession.registerEvent(
                TradeSessionCreatedDomainEvent(
                    Instrument(instrumentId, ticker),
                    candleInterval
                )
            )
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

enum class TradeSessionSort : SortField {

    TICKER, STATUS, START_DATE, CANDLE_INTERVAL

}