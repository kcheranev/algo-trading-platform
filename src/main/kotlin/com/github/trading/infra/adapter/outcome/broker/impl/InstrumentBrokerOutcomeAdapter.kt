package com.github.trading.infra.adapter.outcome.broker.impl

import arrow.core.Either
import arrow.core.Either.Companion.catch
import com.github.trading.common.getOrPut
import com.github.trading.core.error.BrokerIntegrationError
import com.github.trading.core.error.GetShareByIdError
import com.github.trading.core.port.outcome.broker.GetShareByIdCommand
import com.github.trading.core.port.outcome.broker.InstrumentServiceBrokerPort
import com.github.trading.domain.exception.InfrastructureException
import com.github.trading.domain.model.Share
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import ru.tinkoff.piapi.contract.v1.InstrumentIdType
import ru.tinkoff.piapi.contract.v1.InstrumentRequest
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub
import ru.ttech.piapi.core.connector.SyncStubWrapper

private const val INSTRUMENTS_CACHE = "instruments"

@Component
class InstrumentBrokerOutcomeAdapter(
    private val brokerInstrumentServiceWrapper: SyncStubWrapper<InstrumentsServiceBlockingStub>,
    cacheManager: CacheManager
) : InstrumentServiceBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val instrumentsCache =
        cacheManager.getCache(INSTRUMENTS_CACHE)
            ?: throw InfrastructureException("There is no $INSTRUMENTS_CACHE")

    override fun getShareById(command: GetShareByIdCommand): Either<BrokerIntegrationError, Share> =
        catch {
            instrumentsCache.getOrPut(command.instrumentId) {
                brokerInstrumentServiceWrapper.callSyncMethod { stub ->
                    stub.shareBy(
                        InstrumentRequest.newBuilder()
                            .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_UID)
                            .setId(command.instrumentId)
                            .build()
                    )
                }.instrument
            }.let { share -> Share(id = share.uid, ticker = share.ticker, lot = share.lot) }
        }.onLeft { ex -> log.error("An error has been occurred while getting share by id", ex) }
            .mapLeft { GetShareByIdError }

}