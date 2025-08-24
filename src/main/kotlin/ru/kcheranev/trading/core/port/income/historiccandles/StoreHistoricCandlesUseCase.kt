package ru.kcheranev.trading.core.port.income.historiccandles

interface StoreHistoricCandlesUseCase {

    fun storeHistoricCandles(command: StoreHistoricCandlesCommand)

}