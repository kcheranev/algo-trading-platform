package com.github.trading.test.extension

import com.github.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult

class ResetCandleSubscriptionCacheExtension(
    private val candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder
) : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        candleSubscriptionCacheHolder.clear()
    }

}