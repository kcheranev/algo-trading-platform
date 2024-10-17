package ru.kcheranev.trading.infra.adapter.income.web.ui.model.response

import ru.kcheranev.trading.domain.model.CandleInterval

data class CandleSubscriptionUiDto(
    val instrument: InstrumentResponseUiDto,
    val candleInterval: CandleInterval
)