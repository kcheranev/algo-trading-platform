package ru.kcheranev.trading.infra.adapter.income.web.ui.model.response

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.common.InstrumentUiDto

data class CandleSubscriptionUiDto(
    val instrument: InstrumentUiDto,
    val candleInterval: CandleInterval,
    val subscriptionCount: Int
)