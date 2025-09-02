package ru.kcheranev.trading.domain.entity

import org.slf4j.LoggerFactory
import ru.kcheranev.trading.common.date.isTradingTime
import ru.kcheranev.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategy
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
    val orderLotsQuantityStrategy: OrderLotsQuantityStrategy,
    val currentPosition: CurrentPosition = CurrentPosition(),
    var strategy: TradeStrategy,
    val strategyType: String,
    val strategyParameters: StrategyParameters
) : AbstractAggregateRoot() {

    private val log = LoggerFactory.getLogger(javaClass)

    val instrument = Instrument(instrumentId, ticker)

    val about = "[id=${id.value}, ticker=$ticker, strategyType=$strategyType, candleInterval=$candleInterval, status=$status]"

    fun processIncomeCandle(candle: Candle) {
        val isReadyForOrder = status == WAITING || status == IN_POSITION
        if (!isReadyForOrder) {
            throw TradeSessionDomainException(
                "Unable to process income candle for the trade session $about"
            )
        }
        val lastCandleDate = strategy.lastCandleDate()
            ?: throw TradeSessionDomainException("Trade session $about has an empty candle series")
        if (lastCandleDate >= candle.endDateTime) {
            throw TradeSessionDomainException(
                "Unable to process income candle: new candle date intersects trade session $about series dates"
            )
        }
        strategy.addBar(domainModelMapper.map(candle))
        registerEvent(TradeStrategySeriesCandleAddedDomainEvent(this))
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
            TradeSessionPendedForEntryDomainEvent(this)
        )
        log.info("Trade session $about is pended for entry")
    }

    private fun pendingExit() {
        checkTransition(PENDING_EXIT)
        status = PENDING_EXIT
        registerEvent(
            TradeSessionPendedForExitDomainEvent(this)
        )
        log.info("Trade session $about is pended for exit")
    }

    fun enter(lotsRequested: Int, lotsExecuted: Int, averagePrice: BigDecimal) {
        checkTransition(IN_POSITION)
        status = IN_POSITION
        if (lotsRequested != lotsExecuted) {
            log.warn("$lotsExecuted executed lots while entering trade session $about is not equal to expected $lotsRequested")
        }
        currentPosition.enter(lotsExecuted, averagePrice)
        registerEvent(TradeSessionEnteredDomainEvent(this, lotsRequested))
        log.info("Trade session $about has been entered")
    }

    fun exit(lotsExecuted: Int) {
        checkTransition(WAITING)
        status = WAITING
        val lotsRequested = currentPosition.lotsQuantity
        if (lotsRequested != lotsExecuted) {
            log.warn("$lotsExecuted executed lots while exiting trade session $about is not equal to expected $${currentPosition.lotsQuantity}")
        }
        currentPosition.exit()
        registerEvent(TradeSessionExitedDomainEvent(this, lotsRequested, lotsExecuted))
        log.info("Trade session $about has been exited")
    }

    fun resume() {
        val previousStatus = status
        if (havePosition()) {
            checkTransition(IN_POSITION)
            status = IN_POSITION
            registerEvent(TradeSessionResumedDomainEvent(this))
        } else {
            checkTransition(WAITING)
            status = WAITING
            registerEvent(TradeSessionResumedDomainEvent(this))
        }
        log.info("Trade session $about has been resumed, previous status $previousStatus, current status $status")
    }

    private fun havePosition() = currentPosition.lotsQuantity != 0

    fun calculateOrderLotsQuantity() = orderLotsQuantityStrategy.getLotsQuantity(this)

    fun stop() {
        checkTransition(STOPPED)
        status = STOPPED
        registerEvent(TradeSessionStoppedDomainEvent(this))
        log.info("Trade session $about has been stopped")
    }

    fun isMargin() = strategy.margin

    fun lastCandleClosePrice() = strategy.lastCandleClose()
        ?: throw TradeSessionDomainException("Trade session $about has an empty candle series")

    private fun checkTransition(toStatus: TradeSessionStatus) {
        if (!status.transitionAvailable(toStatus)) {
            throw TradeSessionDomainException("Unexpected trade session $about status transition from $status to $toStatus")
        }
    }

    companion object {

        fun create(
            strategyConfiguration: StrategyConfiguration,
            ticker: String,
            instrumentId: String,
            orderLotsQuantityStrategy: OrderLotsQuantityStrategy,
            tradeStrategy: TradeStrategy,
        ): TradeSession {
            val tradeSession =
                TradeSession(
                    id = TradeSessionId.init(),
                    ticker = ticker,
                    instrumentId = instrumentId,
                    status = WAITING,
                    candleInterval = strategyConfiguration.candleInterval,
                    orderLotsQuantityStrategy = orderLotsQuantityStrategy,
                    strategy = tradeStrategy,
                    strategyType = strategyConfiguration.type,
                    strategyParameters = strategyConfiguration.parameters
                )
            tradeSession.registerEvent(TradeSessionCreatedDomainEvent(tradeSession))
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