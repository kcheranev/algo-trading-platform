package com.github.trading.test.extension

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import ru.tinkoff.piapi.core.stream.MarketDataStreamService

class ResetMarketDataStreamExtension(
    private val marketDataStreamService: MarketDataStreamService
) : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        marketDataStreamService.allStreams
            .values
            .forEach {
                it.cancel()
            }
        marketDataStreamService.allStreams.clear()
    }

}