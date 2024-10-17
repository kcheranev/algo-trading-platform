package ru.kcheranev.trading.core.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import ru.kcheranev.trading.core.port.income.marketdata.ProcessCandleUseCase
import ru.kcheranev.trading.core.port.income.marketdata.ProcessIncomeCandleCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.GetReadyToOrderTradeSessionsCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort

@Service
class MarketDataProcessingService(
    private val transactionalTemplate: TransactionTemplate,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
) : ProcessCandleUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun processIncomeCandle(command: ProcessIncomeCandleCommand) {
        val candle = command.candle
        tradeSessionPersistencePort.getReadyForOrderTradeSessions(
            GetReadyToOrderTradeSessionsCommand(candle.instrumentId, candle.interval)
        ).forEach { tradeSession ->
            try {
                transactionalTemplate.execute {
                    tradeSession.processIncomeCandle(candle)
                    tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
                }
            } catch (ex: Exception) {
                log.warn("An error has been occurred while processing income candle", ex)
            }
        }
    }

}