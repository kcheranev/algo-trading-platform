package com.github.trading.test.extension

import com.github.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

class ClearTradeStrategyCacheCacheExtension(
    private val tradeStrategyCache: TradeStrategyCache
) : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        tradeStrategyCache.clear()
    }

}