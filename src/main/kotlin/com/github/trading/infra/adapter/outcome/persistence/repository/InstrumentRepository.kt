package com.github.trading.infra.adapter.outcome.persistence.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import com.github.trading.infra.adapter.outcome.persistence.entity.InstrumentEntity
import java.util.Optional
import java.util.UUID

interface InstrumentRepository : CrudRepository<InstrumentEntity, UUID> {

    @Query("SELECT * FROM instrument WHERE broker_instrument_id = :brokerInstrumentId")
    fun getInstrumentByBrokerInstrumentId(@Param("brokerInstrumentId") brokerInstrumentId: String): Optional<InstrumentEntity>

}