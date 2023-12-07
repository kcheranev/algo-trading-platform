package ru.kcheranev.trading.infra.adapter.income.web.model.response

sealed class SearchResponse

data class OrderSearchResponse(
    var orders: List<OrderDto>
) : SearchResponse()

data class TradeSessionSearchResponse(
    var tradeSessions: List<TradeSessionDto>
) : SearchResponse()

data class StrategyConfigurationSearchResponse(
    var strategyConfigurations: List<StrategyConfigurationDto>
) : SearchResponse()