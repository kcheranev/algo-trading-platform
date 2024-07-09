package ru.kcheranev.trading.core.port.model

enum class Comparsion(
    val value: String
) {

    GT("gt"), LT("lt"), EQ("eq"), GT_EQ("gtEq"), LT_EQ("ltEq")

}