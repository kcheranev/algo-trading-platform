package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.port.outcome.broker.UserServiceBrokerPort
import ru.kcheranev.trading.infra.config.properties.BrokerProperties
import ru.tinkoff.piapi.core.UsersService

@Component
class UserServiceBrokerOutcomeAdapter(
    private val usersService: UsersService,
    brokerProperties: BrokerProperties
) : UserServiceBrokerPort {

    private val tradingAccountName = brokerProperties.tradingAccountName

    override fun getTradingAccountId(): String =
        usersService.accountsSync
            .first { it.name == tradingAccountName }
            .id

}