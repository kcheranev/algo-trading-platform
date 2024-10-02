package ru.kcheranev.trading.core.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.port.income.marketdata.ProcessCandleUseCase
import ru.kcheranev.trading.core.port.income.marketdata.ProcessIncomeCandleCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.GetReadyToOrderTradeSessionsCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort

@Service
class MarketDataProcessingService(
    tradingProperties: TradingProperties,
    private val transactionalTemplate: TransactionTemplate,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
) : ProcessCandleUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    private val availableDelayedCandleCount = tradingProperties.availableDelayedCandleCount

    private val tradingSchedule = tradingProperties.tradingSchedule

    override fun processIncomeCandle(command: ProcessIncomeCandleCommand) {
        val candle = command.candle
        tradeSessionPersistencePort.getReadyForOrderTradeSessions(
            GetReadyToOrderTradeSessionsCommand(candle.instrumentId, candle.interval)
        ).forEach { tradeSession ->
            try {
                transactionalTemplate.execute {
                    tradeSession.processIncomeCandle(candle, availableDelayedCandleCount.toLong(), tradingSchedule)
                    tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
                }
            } catch (ex: Exception) {
                log.warn("An error has been occurred while processing income candle", ex)
            }
        }
    }

}