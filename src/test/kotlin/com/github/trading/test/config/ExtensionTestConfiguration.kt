package com.github.trading.test.config

import com.github.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
import com.github.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import com.github.trading.test.extension.CleanDatabaseExtension
import com.github.trading.test.extension.ClearAllMocksExtension
import com.github.trading.test.extension.ClearCacheExtension
import com.github.trading.test.extension.ClearTradeStrategyCacheCacheExtension
import com.github.trading.test.extension.MockDateSupplierExtension
import com.github.trading.test.extension.ResetCandleSubscriptionCacheExtension
import com.github.trading.test.extension.ResetWireMockExtension
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate

@TestConfiguration
class ExtensionTestConfiguration {

    @Bean
    fun cleanDatabaseExtension(jdbcTemplate: JdbcTemplate) = CleanDatabaseExtension(jdbcTemplate)

    @Bean
    fun clearTradeStrategyCacheExtension(tradeStrategyCache: TradeStrategyCache) =
        ClearTradeStrategyCacheCacheExtension(tradeStrategyCache)

    @Bean
    fun resetCandleSubscriptionsExtension(candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder) =
        ResetCandleSubscriptionCacheExtension(candleSubscriptionCacheHolder)

    @Bean
    fun resetWireMockExtension() = ResetWireMockExtension()

    @Bean
    fun clearAllMockExtension() = ClearAllMocksExtension()

    @Bean
    fun clearCacheExtension(cacheManager: CacheManager) = ClearCacheExtension(cacheManager)

    @Bean
    fun mockDateSupplierExtension() = MockDateSupplierExtension()

}