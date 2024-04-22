package ru.kcheranev.trading.test.util

import java.io.File

fun readResourceAsString(resourcePath: String) =
    File("src/test/resources/$resourcePath").readText(Charsets.UTF_8)