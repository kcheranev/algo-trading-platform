package ru.kcheranev.trading.domain.entity

import org.slf4j.LoggerFactory
import ru.kcheranev.trading.common.date.isTradingTime
import ru.kcheranev.trading.domain.TradeSessionCreatedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionEnteredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExitedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForEntryDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForExitDomainEvent
import ru.kcheranev.trading.domain.TradeSessionResumedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionStoppedDomainEvent
import ru.kcheranev.trading.domain.TradeStrategySeriesCandleAddedDomainEvent
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.IN_POSITION
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.PENDING_ENTER
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.PENDING_EXIT
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.STOPPED
import ru.kcheranev.trading.domain.entity.TradeSessionStatus.WAITING
import ru.kcheranev.trading.domain.exception.TradeSessionDomainException
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.Position
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy
import java.math.BigDecimal
import java.util.UUID

data class TradeSession(
    val id: TradeSessionId,
    val ticker: String,
    val instrumentId: String,
    var status: TradeSessionStatus = WAITING,
    val candleInterval: CandleInterval,
    val lotsQuantity: Int,
    val currentPosition: CurrentPosition = CurrentPosition(),
    var strategy: TradeStrategy,
    val strategyType: String,
    val strategyParameters: StrategyParameters
) : AbstractAggregateRoot() {

    private val log = LoggerFactory.getLogger(javaClass)

    val instrument = Instrument(instrumentId, ticker)

    fun processIncomeCandle(candle: Candle) {
        val isReadyForOrder = status == WAITING || status == IN_POSITION
        if (!isReadyForOrder) {
            throw TradeSessionDomainException(
                "Unable to process income candle for the trade session $this"
            )
        }
        val lastCandleDate =
            strategy.lastCandleDate()
                ?: throw TradeSessionDomainException("Trade session $this has an empty candle series")
        if (lastCandleDate >= candle.endDateTime) {
            throw TradeSessionDomainException(
                "Unable to process income candle: new candle date intersects trade session $this series dates"
            )
        }
        strategy.addBar(domainModelMapper.map(candle))
        registerEvent(TradeStrategySeriesCandleAddedDomainEvent(id))
        executeStrategy()
    }

    private fun executeStrategy() {
        if (shouldEnter()) {
            pendingEnter()
        } else if (shouldExit()) {
            pendingExit()
        }
    }

    private fun shouldEnter() =
        isTradingTime() && status.transitionAvailable(PENDING_ENTER) && strategy.shouldEnter()

    private fun shouldExit() =
        isTradingTime() &&
                status.transitionAvailable(PENDING_EXIT) &&
                strategy.shouldExit(
                    Position(
                        lotsQuantity = currentPosition.lotsQuantity,
                        averagePrice = currentPosition.averagePrice,
                        margin = isMargin()
                    )
                )

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
                lotsQuantityInPosition = currentPosition.lotsQuantity
            )
        )
        log.info("Trade session $this is pended for exit")
    }

    fun enter(lotsExecuted: Int, averagePrice: BigDecimal) {
        checkTransition(IN_POSITION)
        status = IN_POSITION
        if (lotsQuantity != lotsExecuted) {
            log.warn("$lotsExecuted executed lots while entering trade session $this is not equal to expected $lotsQuantity")
        }
        currentPosition.enter(lotsExecuted, averagePrice)
        registerEvent(TradeSessionEnteredDomainEvent(id, instrument, candleInterval))
        log.info("Trade session $this has been entered")
    }

    fun exit(lotsExecuted: Int) {
        checkTransition(WAITING)
        status = WAITING
        if (currentPosition.lotsQuantity != lotsExecuted) {
            log.warn("$lotsExecuted executed lots while exiting trade session $this is not equal to expected $${currentPosition.lotsQuantity}")
        }
        currentPosition.exit()
        registerEvent(TradeSessionExitedDomainEvent(id, instrument, candleInterval))
        log.info("Trade session $this has been exited")
    }

    fun resume() {
        val previousStatus = status
        if (isEntered()) {
            checkTransition(IN_POSITION)
            status = IN_POSITION
            registerEvent(TradeSessionResumedDomainEvent(id, instrument, candleInterval))
        } else {
            checkTransition(WAITING)
            status = WAITING
            registerEvent(TradeSessionResumedDomainEvent(id, instrument, candleInterval))
        }
        log.info("Trade session $this has been resumed, previous status $previousStatus, current status $status")
    }

    private fun isEntered() = currentPosition.lotsQuantity != 0

    fun stop() {
        checkTransition(STOPPED)
        status = STOPPED
        registerEvent(TradeSessionStoppedDomainEvent(id, instrument, candleInterval))
        log.info("Trade session $this has been stopped")
    }

    fun isMargin() = strategy.margin

    private fun checkTransition(toStatus: TradeSessionStatus) {
        if (!status.transitionAvailable(toStatus)) {
            throw TradeSessionDomainException("Unexpected trade session $this status transition from $status to $toStatus")
        }
    }

    override fun toString() =
        "[id=${id.value}, ticker=$ticker, instrumentId=$instrumentId, status=$status, candleInterval=$candleInterval]"

    companion object {

        fun create(
            strategyConfiguration: StrategyConfiguration,
            ticker: String,
            instrumentId: String,
            lotsQuantity: Int,
            tradeStrategy: TradeStrategy,
        ): TradeSession {
            val tradeSession =
                TradeSession(
                    id = TradeSessionId.init(),
                    ticker = ticker,
                    instrumentId = instrumentId,
                    status = WAITING,
                    candleInterval = strategyConfiguration.candleInterval,
                    lotsQuantity = lotsQuantity,
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

data class CurrentPosition(
    var lotsQuantity: Int = 0,
    var averagePrice: BigDecimal = BigDecimal.ZERO
) {

    fun enter(lotsQuantity: Int, averagePrice: BigDecimal) {
        this.lotsQuantity = lotsQuantity
        this.averagePrice = averagePrice
    }

    fun exit() {
        lotsQuantity = 0
        averagePrice = BigDecimal.ZERO
    }

}

enum class TradeSessionStatus(private val availableTransitions: () -> Set<TradeSessionStatus>) {

    WAITING({ setOf(PENDING_ENTER, STOPPED) }),
    PENDING_ENTER({ setOf(WAITING, IN_POSITION, STOPPED) }),
    IN_POSITION({ setOf(PENDING_EXIT, STOPPED) }),
    PENDING_EXIT({ setOf(WAITING, IN_POSITION, STOPPED) }),
    STOPPED({ setOf(WAITING, IN_POSITION) });

    fun transitionAvailable(transition: TradeSessionStatus) = transition in availableTransitions()

}