package ru.kcheranev.trading.test.extension

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionHolder

class ResetCandleSubscriptionCounterExtension(
    private val candleSubscriptionHolder: CandleSubscriptionHolder
) : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        candleSubscriptionHolder.reset()
    }

}