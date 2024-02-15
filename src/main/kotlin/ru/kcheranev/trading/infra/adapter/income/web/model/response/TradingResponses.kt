package ru.kcheranev.trading.infra.adapter.income.web.model.response

sealed class TradingResponses

data class StartTradeSessionResponse(
    val tradeSessionId: Long
) : TradingResponses()