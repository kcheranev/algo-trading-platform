package ru.kcheranev.trading.test.config

import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCounter
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import ru.kcheranev.trading.test.extension.CleanDatabaseExtension
import ru.kcheranev.trading.test.extension.ClearTradeStrategyCacheExtension
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
        ClearTradeStrategyCacheExtension(tradeStrategyCache)

    @Bean
    fun resetCandleSubscriptionCounterExtension(candleSubscriptionCounter: CandleSubscriptionCounter) =
        ResetCandleSubscriptionCounterExtension(candleSubscriptionCounter)

    @Bean
    fun resetWireMockExtension(grpcWireMockServer: WireMockServer) = ResetWireMockExtension(grpcWireMockServer)

    @Bean
    fun resetMarketDataStreamExtension(marketDataStreamService: MarketDataStreamService) =
        ResetMarketDataStreamExtension(marketDataStreamService)

    @Bean
    fun resetTestContextExtensions(
        cleanDatabaseExtension: CleanDatabaseExtension,
        clearTradeStrategyCacheExtension: ClearTradeStrategyCacheExtension,
        resetCandleSubscriptionCounterExtension: ResetCandleSubscriptionCounterExtension,
        resetWireMockExtension: ResetWireMockExtension,
        resetMarketDataStreamExtension: ResetMarketDataStreamExtension
    ) = listOf(
        cleanDatabaseExtension,
        clearTradeStrategyCacheExtension,
        resetCandleSubscriptionCounterExtension,
        resetWireMockExtension,
        resetMarketDataStreamExtension
    )

}