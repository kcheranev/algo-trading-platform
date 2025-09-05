package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.raise.either
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.error.BrokerIntegrationError
import ru.kcheranev.trading.core.error.GetPortfolioError
import ru.kcheranev.trading.core.port.outcome.broker.OperationServiceBrokerPort
import ru.kcheranev.trading.domain.model.Portfolio
import ru.tinkoff.piapi.core.OperationsService

@Component
class OperationServiceBrokerOutcomeAdapter(
    private val userServiceBrokerOutcomeAdapter: UserServiceBrokerOutcomeAdapter,
    private val brokerOperationsService: OperationsService
) : OperationServiceBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getPortfolio(): Either<BrokerIntegrationError, Portfolio> =
        either {
            val accountId = userServiceBrokerOutcomeAdapter.getTradingAccountId().bind()
            catch { brokerOperationsService.getPortfolioSync(accountId) }
                .onLeft { ex -> log.error("An error has been occurred while getting portfolio", ex) }
                .mapLeft { GetPortfolioError }
                .bind()
                .let { portfolioResponse ->
                    Portfolio(
                        currencyAmount = portfolioResponse.totalAmountCurrencies.value,
                        sharesAmount = portfolioResponse.totalAmountShares.value,
                        totalPortfolioAmount = portfolioResponse.totalAmountPortfolio.value
                    )
                }
        }

}