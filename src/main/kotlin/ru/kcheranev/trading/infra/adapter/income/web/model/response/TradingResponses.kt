package ru.kcheranev.trading.infra.adapter.income.web.model.response

import java.util.UUID

sealed class TradingResponses

data class StartTradeSessionResponse(
    val tradeSessionId: UUID
) : TradingResponses()