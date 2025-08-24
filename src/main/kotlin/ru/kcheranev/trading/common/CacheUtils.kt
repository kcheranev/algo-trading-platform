package ru.kcheranev.trading.common

import org.springframework.cache.Cache

@Suppress("UNCHECKED_CAST")
fun <T> Cache.getOrPut(key: String, valueSupplier: () -> T): T =
    get(key)?.get() as T ?: valueSupplier().also { put(key, it) }