package com.github.trading.infra.config

import com.github.trading.infra.adapter.outcome.broker.logging.LoggingOrdersServiceDecorator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub
import ru.tinkoff.piapi.contract.v1.OperationsServiceGrpc
import ru.tinkoff.piapi.contract.v1.OperationsServiceGrpc.OperationsServiceBlockingStub
import ru.tinkoff.piapi.contract.v1.OrdersServiceGrpc
import ru.tinkoff.piapi.contract.v1.OrdersServiceGrpc.OrdersServiceBlockingStub
import ru.tinkoff.piapi.contract.v1.UsersServiceGrpc
import ru.tinkoff.piapi.contract.v1.UsersServiceGrpc.UsersServiceBlockingStub
import ru.ttech.piapi.core.connector.ServiceStubFactory
import ru.ttech.piapi.core.connector.SyncStubWrapper

@Configuration
class BrokerConfiguration {

    @Bean
    fun loggingOrderServiceDecorator(brokerOrdersServiceWrapper: SyncStubWrapper<OrdersServiceBlockingStub>) = LoggingOrdersServiceDecorator(brokerOrdersServiceWrapper)

    @Bean
    fun brokerOrdersServiceWrapper(serviceStubFactory: ServiceStubFactory): SyncStubWrapper<OrdersServiceBlockingStub> =
        serviceStubFactory.newSyncService(OrdersServiceGrpc::newBlockingStub)

    @Bean
    fun brokerUsersServiceWrapper(serviceStubFactory: ServiceStubFactory): SyncStubWrapper<UsersServiceBlockingStub> =
        serviceStubFactory.newSyncService(UsersServiceGrpc::newBlockingStub)

    @Bean
    fun brokerMarketDataServiceWrapper(serviceStubFactory: ServiceStubFactory): SyncStubWrapper<MarketDataServiceBlockingStub> =
        serviceStubFactory.newSyncService(MarketDataServiceGrpc::newBlockingStub)

    @Bean
    fun brokerOperationsServiceWrapper(serviceStubFactory: ServiceStubFactory): SyncStubWrapper<OperationsServiceBlockingStub> =
        serviceStubFactory.newSyncService(OperationsServiceGrpc::newBlockingStub)

    @Bean
    fun brokerInstrumentServiceWrapper(serviceStubFactory: ServiceStubFactory): SyncStubWrapper<InstrumentsServiceBlockingStub> =
        serviceStubFactory.newSyncService(InstrumentsServiceGrpc::newBlockingStub)

}