package ru.kcheranev.trading.test.extension

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.mockk.clearAllMocks

class ClearAllMocksExtension : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        clearAllMocks()
    }

}