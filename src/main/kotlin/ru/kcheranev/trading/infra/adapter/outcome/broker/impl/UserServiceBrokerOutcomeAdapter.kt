package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import arrow.core.Either
import arrow.core.Either.Companion.catch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.error.BrokerIntegrationError
import ru.kcheranev.trading.core.error.GetTradingAccountError
import ru.kcheranev.trading.core.port.outcome.broker.UserServiceBrokerPort
import ru.kcheranev.trading.infra.config.properties.BrokerProperties
import ru.tinkoff.piapi.core.UsersService

@Component
class UserServiceBrokerOutcomeAdapter(
    private val usersService: UsersService,
    brokerProperties: BrokerProperties
) : UserServiceBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val tradingAccountName = brokerProperties.tradingAccountName

    override fun getTradingAccountId(): Either<BrokerIntegrationError, String> =
        catch {
            usersService.accountsSync
                .first { it.name == tradingAccountName }
                .id
        }.onLeft { ex -> log.error("An error has been occurred while getting trading account", ex) }
            .mapLeft { GetTradingAccountError }

}