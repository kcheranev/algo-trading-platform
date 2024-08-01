package ru.kcheranev.trading.infra.adapter.income.web.ui.model.request

data class StrategyParamDto(
    var name: String? = null,
    var value: Number? = null
) {

    constructor(name: String) : this(name, null)

}
