package ru.kcheranev.trading.infra.adapter.income.web.ui

open class WebUiIncomeAdapterException(
    message: String
) : RuntimeException(message)

class NotFoundException(message: String) : WebUiIncomeAdapterException(message)