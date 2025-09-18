package com.github.trading.core.port.income.instrument

interface CreateInstrumentUseCase {

    fun createInstrument(command: CreateInstrumentCommand)

}