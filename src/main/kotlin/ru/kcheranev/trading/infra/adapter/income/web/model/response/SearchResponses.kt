package ru.kcheranev.trading.infra.adapter.income.web.model.response

data class TradeOrderSearchResponseDto(
    var tradeOrders: List<TradeOrderDto>
)

data class TradeSessionSearchResponseDto(
    var tradeSessions: List<TradeSessionDto>
)

data class StrategyConfigurationSearchResponseDto(
    var strategyConfigurations: List<StrategyConfigurationDto>
)