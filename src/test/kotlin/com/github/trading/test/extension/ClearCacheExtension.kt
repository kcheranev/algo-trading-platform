package com.github.trading.test.extension

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.springframework.cache.CacheManager

class ClearCacheExtension(
    private val cacheManager: CacheManager
) : AfterEachListener {

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        cacheManager.cacheNames.forEach {
            cacheManager.getCache(it)?.clear()
        }
    }

}