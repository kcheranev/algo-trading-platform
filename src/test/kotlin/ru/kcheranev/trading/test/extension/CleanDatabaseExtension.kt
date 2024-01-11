package ru.kcheranev.trading.test.extension

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.springframework.jdbc.core.JdbcTemplate

class CleanDatabaseExtension(
    private val jdbcTemplate: JdbcTemplate
) : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        jdbcTemplate.execute("DELETE FROM trade_order")
        jdbcTemplate.execute("DELETE FROM trade_session")
        jdbcTemplate.execute("DELETE FROM strategy_configuration")
    }

}