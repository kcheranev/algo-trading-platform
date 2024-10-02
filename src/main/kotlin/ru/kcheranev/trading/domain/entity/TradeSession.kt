package ru.kcheranev.trading.domain.entity

import org.slf4j.LoggerFactory
import ru.kcheranev.trading.common.date.DateSupplier
import ru.kcheranev.trading.common.date.isWeekend
import ru.kcheranev.trading.common.date.max
import ru.kcheranev.trading.common.date.min
import ru.kcheranev.trading.core.config.TradingScheduleInterval
import ru.kcheranev.trading.domain.DomainException
import ru.kcheranev.trading.domain.TradeSessionCreatedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionDelayedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionDomainException
import ru.kcheranev.trading.domain.TradeSessionEnteredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExitedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionMovedToWaitingForEntryDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForEntryDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForExitDomainEvent
import ru.kcheranev.trading.domain.TradeSessionStoppedDomainEvent
import ru.kcheranev.trading.domain.TradeStrategySeriesCandleAddedDomainEvent
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.DELAYED
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.IN_POSITION
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.PENDING_ENTER
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.PENDING_EXIT
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.STOPPED
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.WAITING
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import java.util.function.Supplier

data class TradeSession(
    val id: TradeSessionId,
    val ticker: String,
    val instrumentId: String,
    var status: TradeSessionStatus = WAITING,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val candleInterval: CandleInterval,
    val lotsQuantity: Int,
    var lotsQuantityInPosition: Int = 0,
    var strategy: TradeStrategy,
    val strategyType: String,
    val strategyParameters: StrategyParameters
) : AbstractAggregateRoot() {

    private val log = LoggerFactory.getLogger(javaClass)

    val instrument = Instrument(instrumentId, ticker)

    fun reinitStrategy(strategy: TradeStrategy) {
        this.strategy = strategy
    }

    fun processIncomeCandle(
        candle: Candle,
        availableDelayedCandleCount: Long,
        tradingSchedule: List<TradingScheduleInterval>
    ) {
        if (!readyForOrder()) {
            throw TradeSessionDomainException(
                "Unable to process income candle for the trade session $this"
            )
        }
        val lastCandleDate = lastCandleDate()
        if (lastCandleDate >= candle.endDateTime) {
            throw TradeSessionDomainException(
                "Unable to process income candle: new candle date intersects trade session $this series dates"
            )
        }
        if (getCandlesCountFromLastCandleDate(candle.endDateTime, tradingSchedule) > availableDelayedCandleCount) {
            delay()
            return
        }
        strategy.addBar(domainModelMapper.map(candle))
        registerEvent(TradeStrategySeriesCandleAddedDomainEvent(id))
        executeStrategy()
    }

    private fun getCandlesCountFromLastCandleDate(
        actualDateTime: LocalDateTime,
        tradingSchedule: List<TradingScheduleInterval>
    ): Int {
        var candlesCount = 0
        val lastCandleDate = lastCandleDate()
        var currentDay = lastCandleDate.toLocalDate()
        while (currentDay <= actualDateTime.toLocalDate()) {
            if (currentDay.isWeekend()) {
                currentDay = currentDay.plusDays(1)
                continue
            }
            val startTime =
                if (currentDay == lastCandleDate.toLocalDate()) {
                    lastCandleDate.toLocalTime()
                } else {
                    LocalTime.MIN
                }
            val endTime =
                if (currentDay == actualDateTime.toLocalDate()) {
                    actualDateTime.toLocalTime()
                } else {
                    LocalTime.MAX
                }
            tradingSchedule.filter { it.afterOrContains(startTime) }
                .filter { it.beforeOrContains(endTime) }
                .map { Duration.between(max(it.from, startTime), min(it.to, endTime)) }
                .forEach { candlesCount += it.dividedBy(candleInterval.duration).toInt() }
            currentDay = currentDay.plusDays(1)
        }
        return candlesCount
    }

    private fun delay() {
        checkTransition(DELAYED)
        status = DELAYED
        registerEvent(TradeSessionDelayedDomainEvent(id))
        log.info("Trade session $this has been delayed")
    }

    private fun readyForOrder() = status == WAITING || status == IN_POSITION

    private fun lastCandleDate() =
        if (strategy.isCandleSeriesEmpty()) {
            throw DomainException("Trade session $this has an empty candle series")
        } else {
            strategy.lastCandleDate()
        }

    private fun executeStrategy() {
        if (shouldEnter()) {
            pendingEnter()
        } else if (shouldExit()) {
            pendingExit()
        }
    }

    private fun shouldEnter() =
        status.transitionAvailable(PENDING_ENTER) && strategy.shouldEnter()

    private fun shouldExit() =
        status.transitionAvailable(PENDING_EXIT) && strategy.shouldExit()

    private fun pendingEnter() {
        checkTransition(PENDING_ENTER)
        status = PENDING_ENTER
        registerEvent(
            TradeSessionPendedForEntryDomainEvent(id, instrument, candleInterval, lotsQuantity)
        )
        log.info("Trade session $this is pended for entry")
    }

    private fun pendingExit() {
        checkTransition(PENDING_EXIT)
        status = PENDING_EXIT
        registerEvent(
            TradeSessionPendedForExitDomainEvent(
                tradeSessionId = id,
                instrument = instrument,
                candleInterval = candleInterval,
                lotsQuantityInPosition = lotsQuantityInPosition
            )
        )
        log.info("Trade session $this is pended for exit")
    }

    fun enter(lotsExecuted: Int) {
        checkTransition(IN_POSITION)
        status = IN_POSITION
        if (lotsQuantity != lotsExecuted) {
            log.warn("$lotsExecuted executed lots while entering trade session $this is not equal to expected $lotsQuantity")
        }
        lotsQuantityInPosition = lotsExecuted
        registerEvent(TradeSessionEnteredDomainEvent(id, instrument, candleInterval))
        log.info("Trade session $this has been entered")
    }

    fun exit(lotsExecuted: Int) {
        checkTransition(WAITING)
        status = WAITING
        if (lotsQuantityInPosition != lotsExecuted) {
            log.warn("$lotsExecuted executed lots while exiting trade session $this is not equal to expected $lotsQuantityInPosition")
        }
        lotsQuantityInPosition = 0
        registerEvent(TradeSessionExitedDomainEvent(id, instrument, candleInterval))
        log.info("Trade session $this has been exited")
    }

    fun await() {
        when (status) {
            PENDING_ENTER -> {
                checkTransition(WAITING)
                status = WAITING
                registerEvent(TradeSessionMovedToWaitingForEntryDomainEvent(id))
                log.info("Trade session $this has been moved back from PENDING_ENTER")
            }

            PENDING_EXIT -> {
                checkTransition(IN_POSITION)
                status = IN_POSITION
                registerEvent(TradeSessionMovedToWaitingForEntryDomainEvent(id))
                log.info("Trade session $this has been moved back from PENDING_EXIT")
            }

            DELAYED -> {
                if (isEntered()) {
                    checkTransition(IN_POSITION)
                    status = IN_POSITION
                    registerEvent(TradeSessionMovedToWaitingForEntryDomainEvent(id))
                    log.info("Trade session $this has been moved back from DELAYED")
                } else {
                    checkTransition(WAITING)
                    status = WAITING
                    registerEvent(TradeSessionMovedToWaitingForEntryDomainEvent(id))
                    log.info("Trade session $this has been moved back from DELAYED")
                }
            }

            else -> throw TradeSessionDomainException("Trade session $this unable to move to waiting for entry status")
        }
    }

    private fun isEntered() = lotsQuantityInPosition != 0

    fun stop() {
        checkTransition(STOPPED)
        status = STOPPED
        registerEvent(TradeSessionStoppedDomainEvent(id, instrument, candleInterval))
        log.info("Trade session $this has been stopped")
    }

    fun isMargin() = strategy.margin

    fun isTerminal() = status.terminal

    private fun checkTransition(toStatus: TradeSessionStatus) {
        if (!status.transitionAvailable(toStatus)) {
            throw TradeSessionDomainException("Unexpected trade session $this status transition from $status to $toStatus")
        }
    }

    override fun toString() =
        "[id=${id.value}, ticker=$ticker, instrumentId=$instrumentId, status=$status, candleInterval=$candleInterval]"

    companion object {

        fun start(
            strategyConfiguration: StrategyConfiguration,
            ticker: String,
            instrumentId: String,
            lotsQuantity: Int,
            tradeStrategy: TradeStrategy,
            dateSupplier: DateSupplier
        ): TradeSession {
            val tradeSession =
                TradeSession(
                    id = TradeSessionId.init(),
                    ticker = ticker,
                    instrumentId = instrumentId,
                    status = WAITING,
                    startDate = dateSupplier.currentDateTime(),
                    candleInterval = strategyConfiguration.candleInterval,
                    lotsQuantity = lotsQuantity,
                    lotsQuantityInPosition = 0,
                    strategy = tradeStrategy,
                    strategyType = strategyConfiguration.type,
                    strategyParameters = strategyConfiguration.parameters
                )
            tradeSession.registerEvent(
                TradeSessionCreatedDomainEvent(
                    Instrument(instrumentId, ticker),
                    strategyConfiguration.candleInterval
                )
            )
            return tradeSession
        }

    }

}

data class TradeSessionId(
    val value: UUID
) {

    override fun toString() = value.toString()

    companion object {

        fun init() = TradeSessionId(UUID.randomUUID())

    }

}

enum class TradeSessionStatus(
    private val availableTransitions: Supplier<Set<TradeSessionStatus>>,
    val terminal: Boolean
) {

    WAITING({ setOf(PENDING_ENTER, STOPPED, DELAYED) }, false),
    PENDING_ENTER({ setOf(IN_POSITION, STOPPED, WAITING) }, false),
    IN_POSITION({ setOf(PENDING_EXIT, STOPPED, DELAYED) }, false),
    PENDING_EXIT({ setOf(WAITING, STOPPED, IN_POSITION) }, false),
    DELAYED({ setOf(WAITING, IN_POSITION, STOPPED) }, false),
    STOPPED({ emptySet() }, true);

    fun transitionAvailable(transition: TradeSessionStatus) = transition in availableTransitions.get()

}