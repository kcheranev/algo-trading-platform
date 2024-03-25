package ru.kcheranev.trading.test.extension

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeSessionCache

class ClearTradeSessionCacheExtension(
    private val tradeSessionCache: TradeSessionCache
) : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        tradeSessionCache.clear()
    }

}