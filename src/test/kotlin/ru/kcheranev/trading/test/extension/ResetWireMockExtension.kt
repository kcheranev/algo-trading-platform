package ru.kcheranev.trading.test.extension

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import ru.kcheranev.trading.test.stub.WireMockServers.grpcWireMockServer
import ru.kcheranev.trading.test.stub.WireMockServers.httpWireMockServer

class ResetWireMockExtension : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        httpWireMockServer.resetAll()
        grpcWireMockServer.resetAll()
    }

}