package ru.kcheranev.trading.domain.entity

import org.slf4j.LoggerFactory
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeriesBuilder
import ru.kcheranev.trading.common.DateSupplier
import ru.kcheranev.trading.core.strategy.StrategyFactory
import ru.kcheranev.trading.domain.TradeSessionCreatedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionDomainException
import ru.kcheranev.trading.domain.TradeSessionEnteredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExitedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExpiredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForEntryDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForExitDomainEvent
import ru.kcheranev.trading.domain.TradeSessionStoppedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionStrategySeriesRefreshedDomainEvent
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
    val strategy: TradeStrategy?,
    val strategyConfigurationId: StrategyConfigurationId
) : AbstractAggregateRoot() {

    private val log = LoggerFactory.getLogger(javaClass)

    val instrument = Instrument(instrumentId, ticker)

    val candleIntervalDuration = candleInterval.duration

    fun executeStrategy() {
        if (shouldEnter()) {
            pendingEnter()
        } else if (shouldExit()) {
            pendingExit()
        }
    }

    private fun shouldEnter() =
        strategy != null && status.transitionAvailable(PENDING_ENTER) && strategy.shouldEnter(strategy.series.endIndex)

    private fun shouldExit() =
        strategy != null && status.transitionAvailable(PENDING_EXIT) && strategy.shouldExit(strategy.series.endIndex)

    private fun pendingEnter() {
        checkExists()
        checkStrategyExists()
        checkTransition(PENDING_ENTER)
        status = PENDING_ENTER
        registerEvent(
            TradeSessionPendedForEntryDomainEvent(id!!, instrument, candleInterval, lotsQuantity)
        )
        log.info("Trade session ${id.value} is pended for entry")
    }

    private fun pendingExit() {
        checkExists()
        checkStrategyExists()
        checkTransition(PENDING_EXIT)
        status = PENDING_EXIT
        registerEvent(
            TradeSessionPendedForExitDomainEvent(
                id!!,
                instrument,
                candleInterval,
                lotsQuantity
            )
        )
        log.info("Trade session ${id.value} is pended for exit")
    }

    fun enter() {
        checkExists()
        checkStrategyExists()
        checkTransition(IN_POSITION)
        status = IN_POSITION
        registerEvent(TradeSessionEnteredDomainEvent(id!!, instrument, candleInterval))
        log.info("Trade session ${id.value} has been entered")
    }

    fun exit() {
        checkExists()
        checkStrategyExists()
        checkTransition(WAITING)
        status = WAITING
        registerEvent(TradeSessionExitedDomainEvent(id!!, instrument, candleInterval))
        log.info("Trade session ${id.value} has been exited")
    }

    fun lastCandleDate(): LocalDateTime {
        checkStrategyExists()
        return strategy!!.series
            .lastBar
            .endTime
            .toLocalDateTime()
    }

    fun freshCandleSeries(availableCandleDelay: Long, dateSupplier: DateSupplier): Boolean {
        checkStrategyExists()
        return lastCandleDate().plus(candleIntervalDuration.multipliedBy(availableCandleDelay)) >=
                dateSupplier.currentDate().truncatedTo(candleInterval.chronoUnit)
    }

    fun expiredCandleSeries(maxCandleDelay: Long, dateSupplier: DateSupplier): Boolean {
        checkStrategyExists()
        return lastCandleDate().plus(candleIntervalDuration.multipliedBy(maxCandleDelay)) <
                dateSupplier.currentDate().truncatedTo(candleInterval.chronoUnit)
    }

    fun addCandle(candle: Candle, availableCandleDelay: Long) {
        checkExists()
        checkStrategyExists()
        checkWaitingForNewCandle()
        val lastCandleDate = lastCandleDate()
        if (lastCandleDate >= candle.endTime) {
            throw TradeSessionDomainException(
                "Unable to add candle to the trade session ${id!!.value} strategy series: new candle date intersects series dates"
            )
        }
        if (lastCandleDate.plus(candleIntervalDuration.multipliedBy(availableCandleDelay)) < candle.endTime) {
            throw TradeSessionDomainException(
                "Unable to add candle to the trade session ${id!!.value} strategy series: series is delayed"
            )
        }
        strategy!!.addBar(domainModelMapper.map(candle))
        registerEvent(TradeStrategySeriesCandleAddedDomainEvent(id!!, candle, instrument))
    }

    fun refreshCandleSeries(candles: List<Candle>) {
        checkExists()
        checkStrategyExists()
        checkWaitingForNewCandle()
        if (lastCandleDate() >= candles.first().endTime) {
            throw TradeSessionDomainException(
                "Unable to refresh trade session ${id!!.value} strategy series: new candles dates intersect series dates"
            )
        }
        candles.forEach { strategy!!.addBar(domainModelMapper.map(it)) }
        registerEvent(TradeSessionStrategySeriesRefreshedDomainEvent(id!!, instrument, candleInterval))
        log.info("Trade session ${id.value} strategy series has been refreshed")
    }

    fun expire() {
        checkExists()
        checkTransition(EXPIRED)
        status = EXPIRED
        registerEvent(TradeSessionExpiredDomainEvent(id!!, instrument, candleInterval))
        log.info("Trade session ${id.value} has been expired")
    }

    fun stop() {
        checkExists()
        checkTransition(STOPPED)
        status = STOPPED
        registerEvent(TradeSessionStoppedDomainEvent(id!!, instrument, candleInterval))
        log.info("Trade session ${id.value} has been stopped")
    }

    private fun checkExists() {
        if (id == null) {
            throw TradeSessionDomainException("Trade session is not exists")
        }
    }

    private fun checkStrategyExists() {
        if (strategy == null) {
            throw TradeSessionDomainException("Trade session ${id?.value} strategy not found")
        }
    }

    private fun checkWaitingForNewCandle() {
        if (status != WAITING && status != IN_POSITION) {
            throw TradeSessionDomainException(
                "Unable to add candle to the trade session ${id?.value} strategy series, current trade session status is $status"
            )
        }
    }

    private fun checkTransition(toStatus: TradeSessionStatus) {
        if (!status.transitionAvailable(toStatus)) {
            throw TradeSessionDomainException("Unexpected trade session ${id?.value} status transition from $status to $toStatus")
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

    WAITING({ setOf(PENDING_ENTER, STOPPED, EXPIRED) }),
    PENDING_ENTER({ setOf(IN_POSITION, STOPPED) }),
    IN_POSITION({ setOf(PENDING_EXIT, STOPPED, EXPIRED) }),
    PENDING_EXIT({ setOf(WAITING, STOPPED) }),
    STOPPED({ emptySet() }),
    EXPIRED({ emptySet() });

    fun transitionAvailable(transition: TradeSessionStatus) = transition in availableTransitions.get()

}