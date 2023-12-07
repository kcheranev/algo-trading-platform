package ru.kcheranev.trading.core.port.common.model

enum class Comparsion(
    val value: String
) {

    GT("gt"), LT("lt"), EQ("eq"), GT_EQ("gtEq"), LT_EQ("ltEq")

}