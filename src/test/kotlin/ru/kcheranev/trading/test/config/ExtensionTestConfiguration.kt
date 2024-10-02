package ru.kcheranev.trading.test.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionHolder
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import ru.kcheranev.trading.test.extension.CleanDatabaseExtension
import ru.kcheranev.trading.test.extension.ClearTradeStrategyCacheCacheExtension
import ru.kcheranev.trading.test.extension.ResetCandleSubscriptionCounterExtension
import ru.kcheranev.trading.test.extension.ResetMarketDataStreamExtension
import ru.kcheranev.trading.test.extension.ResetWireMockExtension
import ru.tinkoff.piapi.core.stream.MarketDataStreamService

@TestConfiguration
class ExtensionTestConfiguration {

    @Bean
    fun cleanDatabaseExtension(jdbcTemplate: JdbcTemplate) = CleanDatabaseExtension(jdbcTemplate)

    @Bean
    fun clearTradeStrategyCacheExtension(tradeStrategyCache: TradeStrategyCache) =
        ClearTradeStrategyCacheCacheExtension(tradeStrategyCache)

    @Bean
    fun resetCandleSubscriptionCounterExtension(candleSubscriptionHolder: CandleSubscriptionHolder) =
        ResetCandleSubscriptionCounterExtension(candleSubscriptionHolder)

    @Bean
    fun resetWireMockExtension() = ResetWireMockExtension()

    @Bean
    fun resetMarketDataStreamExtension(marketDataStreamService: MarketDataStreamService) =
        ResetMarketDataStreamExtension(marketDataStreamService)

    @Bean
    fun resetTestContextExtensions(
        cleanDatabaseExtension: CleanDatabaseExtension,
        clearTradeStrategyCacheCacheExtension: ClearTradeStrategyCacheCacheExtension,
        resetCandleSubscriptionCounterExtension: ResetCandleSubscriptionCounterExtension,
        resetWireMockExtension: ResetWireMockExtension,
        resetMarketDataStreamExtension: ResetMarketDataStreamExtension
    ) = listOf(
        cleanDatabaseExtension,
        clearTradeStrategyCacheCacheExtension,
        resetCandleSubscriptionCounterExtension,
        resetWireMockExtension,
        resetMarketDataStreamExtension
    )

}