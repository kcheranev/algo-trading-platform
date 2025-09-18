package com.github.trading.test.extension

import com.github.trading.test.stub.WireMockServers.grpcWireMockServer
import com.github.trading.test.stub.WireMockServers.httpWireMockServer
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

class ResetWireMockExtension : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        httpWireMockServer.resetAll()
        grpcWireMockServer.resetAll()
    }

}