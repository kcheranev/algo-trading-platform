package ru.kcheranev.trading.core.port.model.sort

interface SortField

enum class StrategyConfigurationSort : SortField {

    TYPE, CANDLE_INTERVAL

}

enum class TradeOrderSort : SortField {

    TICKER, DATE, TOTAL_PRICE, DIRECTION

}

enum class TradeSessionSort : SortField {

    TICKER, STATUS, START_DATE, CANDLE_INTERVAL

}