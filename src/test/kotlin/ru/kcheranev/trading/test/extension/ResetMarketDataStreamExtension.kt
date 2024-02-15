package ru.kcheranev.trading.test.extension

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import ru.kcheranev.trading.infra.config.BrokerApi

class ResetMarketDataStreamExtension(
    brokerApi: BrokerApi
) : AfterEachListener {

    private val marketDataStreamService = brokerApi.marketDataStreamService

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        marketDataStreamService.allStreams.clear()
    }

}