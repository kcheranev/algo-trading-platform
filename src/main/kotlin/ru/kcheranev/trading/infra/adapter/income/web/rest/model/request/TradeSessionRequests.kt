package ru.kcheranev.trading.infra.adapter.income.web.rest.model.request

import io.swagger.v3.oas.annotations.media.Schema
import ru.kcheranev.trading.core.port.model.Page
import ru.kcheranev.trading.core.port.model.sort.Sort
import ru.kcheranev.trading.core.port.model.sort.TradeSessionSort
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import java.util.UUID

data class CreateTradeSessionRequestDto(
    @Schema(description = "Strategy configuration id") val strategyConfigurationId: UUID,
    @Schema(description = "Lots quantity") val lotsQuantity: Int,
    @Schema(description = "Instrument") val instrument: InstrumentDto
)

data class SearchTradeSessionRequestDto(
    val id: UUID? = null,
    val ticker: String? = null,
    val instrumentId: String? = null,
    val status: TradeSessionStatus? = null,
    val candleInterval: CandleInterval? = null,
    val page: Page? = null,
    val sort: Sort<TradeSessionSort>? = null
)