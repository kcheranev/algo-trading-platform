package ru.kcheranev.trading.core.port.income.tradesession

interface ResumeTradeSessionUseCase {

    fun reinitStrategy(command: ReinitStrategyCommand)

}