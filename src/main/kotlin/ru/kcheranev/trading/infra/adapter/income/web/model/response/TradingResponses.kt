package ru.kcheranev.trading.infra.adapter.income.web.model.response

import java.util.UUID

data class StartTradeSessionResponseDto(
    val tradeSessionId: UUID
)