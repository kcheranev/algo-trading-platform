package ru.kcheranev.trading.core.port.income.instrument

import ru.kcheranev.trading.domain.entity.Instrument

interface FindAllInstrumentsUseCase {

    fun findAll(): List<Instrument>

}