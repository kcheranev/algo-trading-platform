package ru.kcheranev.trading.domain.entity

import org.slf4j.LoggerFactory
import org.ta4j.core.BaseBarSeriesBuilder
import ru.kcheranev.trading.common.DateSupplier
import ru.kcheranev.trading.core.strategy.factory.StrategyFactory
import ru.kcheranev.trading.domain.DomainException
import ru.kcheranev.trading.domain.TradeSessionCreatedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionDomainException
import ru.kcheranev.trading.domain.TradeSessionEnteredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExitedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExpiredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionMovedToWaitingForEntryDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForEntryDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForExitDomainEvent
import ru.kcheranev.trading.domain.TradeSessionStoppedDomainEvent
import ru.kcheranev.trading.domain.TradeStrategySeriesCandleAddedDomainEvent
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.EXPIRED
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.IN_POSITION
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.PENDING_ENTER
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.PENDING_EXIT
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.STOPPED
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.WAITING
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.TradeStrategy
import java.time.LocalDateTime
import java.util.UUID
import java.util.function.Supplier

private const val MAX_STRATEGY_BARS_COUNT = 200

data class TradeSession(
    val id: TradeSessionId?,
    val ticker: String,
    val instrumentId: String,
    var status: TradeSessionStatus = WAITING,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val candleInterval: CandleInterval,
    val lotsQuantity: Int,
    var lotsQuantityInPosition: Int = 0,
    val strategy: TradeStrategy,
    val strategyConfigurationId: StrategyConfigurationId
) : AbstractAggregateRoot() {

    private val log = LoggerFactory.getLogger(javaClass)

    val instrument = Instrument(instrumentId, ticker)

    fun initStrategySeries(candles: List<Candle>) {
        if (strategy.series.barCount != 0) {
            throw DomainException("Trade session $this strategy already initialized")
        }
        candles.forEach { strategy.addBar(domainModelMapper.map(it)) }
    }

    fun processIncomeCandle(candle: Candle, availableCandleDelay: Long) {
        if (!readyForOrder()) {
            throw TradeSessionDomainException(
                "Unable to process income candle for the trade session $this"
            )
        }
        val lastCandleDate = lastCandleDate()
        if (lastCandleDate >= candle.endTime) {
            throw TradeSessionDomainException(
                "Unable to process income candle: new candle date intersects trade session $this series dates"
            )
        }
        if (lastCandleDate + candleInterval.duration.multipliedBy(availableCandleDelay) < candle.endTime) {
            expire()
            return
        }
        strategy.addBar(domainModelMapper.map(candle))
        registerEvent(TradeStrategySeriesCandleAddedDomainEvent(id!!))
        executeStrategy()
    }

    fun readyForOrder() = status == WAITING || status == IN_POSITION

    private fun lastCandleDate() =
        strategy.series
            .lastBar
            .endTime
            .toLocalDateTime()

    private fun expire() {
        checkTransition(EXPIRED)
        status = EXPIRED
        registerEvent(TradeSessionExpiredDomainEvent(id!!, instrument, candleInterval))
        log.info("Trade session $this has been expired")
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
            TradeSessionPendedForEntryDomainEvent(id!!, instrument, candleInterval, lotsQuantity)
        )
        log.info("Trade session $this is pended for entry")
    }

    private fun pendingExit() {
        checkTransition(PENDING_EXIT)
        status = PENDING_EXIT
        registerEvent(
            TradeSessionPendedForExitDomainEvent(
                tradeSessionId = id!!,
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
        registerEvent(TradeSessionEnteredDomainEvent(id!!, instrument, candleInterval))
        log.info("Trade session $this has been entered")
    }

    fun exit(lotsExecuted: Int) {
        checkTransition(WAITING)
        status = WAITING
        if (lotsQuantityInPosition != lotsExecuted) {
            log.warn("$lotsExecuted executed lots while exiting trade session $this is not equal to expected $lotsQuantityInPosition")
        }
        lotsQuantityInPosition = 0
        registerEvent(TradeSessionExitedDomainEvent(id!!, instrument, candleInterval))
        log.info("Trade session $this has been exited")
    }

    fun waitForEntry() {
        if (status == PENDING_ENTER) {
            checkTransition(WAITING)
            status = WAITING
            registerEvent(TradeSessionMovedToWaitingForEntryDomainEvent(id!!))
            log.info("Trade session $this has been moved back from $PENDING_ENTER")
            return
        } else if (status == PENDING_EXIT) {
            checkTransition(IN_POSITION)
            status = IN_POSITION
            registerEvent(TradeSessionMovedToWaitingForEntryDomainEvent(id!!))
            log.info("Trade session $this has been moved back from $PENDING_EXIT")
            return
        }
        throw TradeSessionDomainException("Trade session $this unable to move to waiting for entry status")
    }

    fun stop() {
        checkTransition(STOPPED)
        status = STOPPED
        registerEvent(TradeSessionStoppedDomainEvent(id!!, instrument, candleInterval))
        log.info("Trade session $this has been stopped")
    }

    fun margin() = strategy.margin

    private fun checkTransition(toStatus: TradeSessionStatus) {
        if (!status.transitionAvailable(toStatus)) {
            throw TradeSessionDomainException("Unexpected trade session $this status transition from $status to $toStatus")
        }
    }

    override fun toString() =
        "[id=${id?.value}, ticker=$ticker, instrumentId=$instrumentId, status=$status, candleInterval=$candleInterval]"

    companion object {

        fun start(
            strategyConfiguration: StrategyConfiguration,
            ticker: String,
            instrumentId: String,
            lotsQuantity: Int,
            strategyFactory: StrategyFactory,
            dateSupplier: DateSupplier
        ): TradeSession {
            val candleInterval = strategyConfiguration.candleInterval
            val series =
                BaseBarSeriesBuilder()
                    .withName("Trade session: ticker=$ticker, candleInterval=$candleInterval")
                    .withMaxBarCount(MAX_STRATEGY_BARS_COUNT)
                    .build()
            val tradeStrategy =
                strategyFactory.initStrategy(
                    strategyConfiguration.params,
                    CustomizedBarSeries(series, candleInterval)
                )
            val tradeSession =
                TradeSession(
                    id = null,
                    ticker = ticker,
                    instrumentId = instrumentId,
                    status = WAITING,
                    startDate = dateSupplier.currentDate(),
                    candleInterval = candleInterval,
                    lotsQuantity = lotsQuantity,
                    lotsQuantityInPosition = 0,
                    strategy = tradeStrategy,
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
    val value: UUID
)

enum class TradeSessionStatus(
    private val availableTransitions: Supplier<Set<TradeSessionStatus>>,
    val terminal: Boolean
) {

    WAITING({ setOf(PENDING_ENTER, STOPPED, EXPIRED) }, false),
    PENDING_ENTER({ setOf(IN_POSITION, STOPPED, WAITING) }, false),
    IN_POSITION({ setOf(PENDING_EXIT, STOPPED, EXPIRED) }, false),
    PENDING_EXIT({ setOf(WAITING, STOPPED, IN_POSITION) }, false),
    STOPPED({ emptySet() }, true),
    EXPIRED({ emptySet() }, true);

    fun transitionAvailable(transition: TradeSessionStatus) = transition in availableTransitions.get()

}