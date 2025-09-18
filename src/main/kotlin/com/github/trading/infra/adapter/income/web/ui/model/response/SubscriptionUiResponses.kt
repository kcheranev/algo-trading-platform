package com.github.trading.infra.adapter.income.web.ui.model.response

import com.github.trading.domain.model.CandleInterval

data class CandleSubscriptionUiDto(
    val instrument: InstrumentResponseUiDto,
    val candleInterval: CandleInterval
)