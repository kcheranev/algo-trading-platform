package ru.kcheranev.trading.test.extension

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.listeners.BeforeEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import ru.kcheranev.trading.common.date.DateSupplier
import java.time.LocalDate
import java.time.LocalDateTime

class MockDateSupplierExtension : BeforeEachListener, AfterEachListener {

    override suspend fun beforeEach(testCase: TestCase) {
        mockkObject(DateSupplier)
        every { DateSupplier.currentDateTime() } returns LocalDateTime.parse("2024-01-30T10:15:30")
        every { DateSupplier.currentDate() } returns LocalDate.parse("2024-01-30")
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        unmockkObject(DateSupplier)
    }

}