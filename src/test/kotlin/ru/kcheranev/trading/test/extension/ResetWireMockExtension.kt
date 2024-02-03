package ru.kcheranev.trading.test.extension

import com.github.tomakehurst.wiremock.WireMockServer
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

class ResetWireMockExtension(
    private val grpcWireMockServer: WireMockServer
) : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        grpcWireMockServer.resetAll()
    }

}