package ru.kcheranev.trading.core.port.income.instrument

interface CreateInstrumentUseCase {

    fun createInstrument(command: CreateInstrumentCommand)

}