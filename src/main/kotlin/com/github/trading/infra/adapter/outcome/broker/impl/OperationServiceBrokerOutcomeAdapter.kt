package com.github.trading.infra.adapter.outcome.broker.impl

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.raise.either
import com.github.trading.core.error.IntegrationError.BrokerIntegrationError
import com.github.trading.core.port.outcome.broker.OperationServiceBrokerPort
import com.github.trading.domain.model.Portfolio
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.tinkoff.piapi.contract.v1.OperationsServiceGrpc.OperationsServiceBlockingStub
import ru.tinkoff.piapi.contract.v1.PortfolioRequest
import ru.ttech.piapi.core.connector.SyncStubWrapper
import ru.ttech.piapi.core.helpers.NumberMapper.moneyValueToBigDecimal

@Component
class OperationServiceBrokerOutcomeAdapter(
    private val userServiceBrokerOutcomeAdapter: UserServiceBrokerOutcomeAdapter,
    private val brokerOperationsServiceWrapper: SyncStubWrapper<OperationsServiceBlockingStub>
) : OperationServiceBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getPortfolio(): Either<BrokerIntegrationError, Portfolio> =
        either {
            val accountId = userServiceBrokerOutcomeAdapter.getTradingAccountId().bind()
            catch {
                brokerOperationsServiceWrapper.callSyncMethod { stub ->
                    stub.getPortfolio(
                        PortfolioRequest.newBuilder()
                            .setAccountId(accountId)
                            .build()
                    )
                }
            }.onLeft { ex -> log.error("An error has been occurred while getting portfolio", ex) }
                .mapLeft { BrokerIntegrationError.GetPortfolioError }
                .bind()
        }.map { portfolioResponse ->
            Portfolio(
                currencyAmount = moneyValueToBigDecimal(portfolioResponse.totalAmountCurrencies),
                sharesAmount = moneyValueToBigDecimal(portfolioResponse.totalAmountShares),
                totalPortfolioAmount = moneyValueToBigDecimal(portfolioResponse.totalAmountPortfolio)
            )
        }

}