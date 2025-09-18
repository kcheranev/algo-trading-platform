package com.github.trading.common

object ResourceUtils {

    fun readResourceAsString(filePath: String) =
        javaClass.getResourceAsStream(filePath)
            ?.bufferedReader()
            ?.readText()
            ?: throw IllegalArgumentException("There is no resource $filePath")

}