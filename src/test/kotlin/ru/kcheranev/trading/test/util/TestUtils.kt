package ru.kcheranev.trading.test.util

import java.io.File

object TestUtils {

    fun readResourceAsString(resourcePath: String) =
        File("src/test/resources/$resourcePath").readText(Charsets.UTF_8)

}