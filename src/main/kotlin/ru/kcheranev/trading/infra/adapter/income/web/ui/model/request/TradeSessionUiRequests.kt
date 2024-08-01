package ru.kcheranev.trading.infra.adapter.income.web.ui.model.request

import ru.kcheranev.trading.core.port.model.sort.Sort
import ru.kcheranev.trading.core.port.model.sort.TradeSessionSort
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import java.util.UUID

data class StartTradeSessionRequestUiDto(
    val strategyConfigurationId: UUID,
    val lotsQuantity: Int,
    val instrument: InstrumentUiRequestDto
)

data class SearchTradeSessionRequestUiDto(
    val id: UUID? = null,
    val ticker: String? = null,
    val instrumentId: String? = null,
    val status: TradeSessionStatus? = null,
    val candleInterval: CandleInterval? = null,
    val sort: Sort<TradeSessionSort>? = null
)