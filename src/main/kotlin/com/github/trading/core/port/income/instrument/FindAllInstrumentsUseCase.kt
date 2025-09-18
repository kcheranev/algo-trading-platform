package com.github.trading.core.port.income.instrument

import com.github.trading.domain.entity.Instrument

interface FindAllInstrumentsUseCase {

    fun findAll(): List<Instrument>

}