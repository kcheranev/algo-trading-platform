package ru.kcheranev.trading.infra.adapter.income.web.ui.model.response

import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import java.util.UUID

data class TradeSessionUiDto(
    val id: UUID,
    val ticker: String,
    val instrumentId: String,
    val status: TradeSessionStatus,
    val candleInterval: CandleInterval,
    val lotsQuantity: Int,
    val lotsQuantityInPosition: Int,
    val strategyType: String,
    val strategyParameters: Map<String, Number>
) {

    val availableToStop = status != TradeSessionStatus.STOPPED

    val availableToResume =
        status in listOf(TradeSessionStatus.PENDING_ENTER, TradeSessionStatus.PENDING_EXIT, TradeSessionStatus.STOPPED)

}