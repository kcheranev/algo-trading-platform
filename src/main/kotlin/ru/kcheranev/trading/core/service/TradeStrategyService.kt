package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import org.ta4j.core.BaseBarSeriesBuilder
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.port.service.TradeStrategyServicePort
import ru.kcheranev.trading.core.port.service.command.InitTradeStrategyCommand
import ru.kcheranev.trading.core.strategy.factory.StrategyFactoryProvider
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.TradeStrategy

private const val MAX_STRATEGY_BARS_COUNT = 200

private const val DEFAULT_INIT_CANDLES_AMOUNT = 10

@Service
class TradeStrategyService(
    private val strategyFactoryProvider: StrategyFactoryProvider,
    private val historicCandleBrokerPort: HistoricCandleBrokerPort
) : TradeStrategyServicePort {

    override fun initTradeStrategy(command: InitTradeStrategyCommand): TradeStrategy {
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        val series =
            BaseBarSeriesBuilder()
                .withName("Trade session: ticker=${command.instrument.ticker}, candleInterval=${command.candleInterval}")
                .withMaxBarCount(MAX_STRATEGY_BARS_COUNT)
                .build()
        val tradeStrategy =
            strategyFactory.initStrategy(
                command.strategyParameters,
                CustomizedBarSeries(series)
            )
        val initCandlesAmount =
            if (tradeStrategy.unstableBars == 0) {
                DEFAULT_INIT_CANDLES_AMOUNT
            } else {
                tradeStrategy.unstableBars
            }
        val candles =
            historicCandleBrokerPort.getLastHistoricCandles(
                GetLastHistoricCandlesCommand(command.instrument, command.candleInterval, initCandlesAmount)
            )
        candles.forEach { tradeStrategy.addBar(domainModelMapper.map(it)) }
        return tradeStrategy
    }

}