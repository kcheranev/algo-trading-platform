package ru.kcheranev.trading.infra.adapter.outcome.broker

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.outcome.broker.model.InstrumentResponse
import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse
import ru.tinkoff.piapi.contract.v1.Instrument

@Mapper
interface BrokerOutcomeAdapterMapper {

    fun map(source: ru.tinkoff.piapi.contract.v1.PostOrderResponse): PostOrderResponse

    fun map(source: Instrument): InstrumentResponse

}

val brokerOutcomeAdapterMapper: BrokerOutcomeAdapterMapper = Mappers.getMapper(BrokerOutcomeAdapterMapper::class.java)