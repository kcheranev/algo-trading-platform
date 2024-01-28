package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.infra.config.BrokerApi
import ru.kcheranev.trading.infra.config.BrokerProperties

@Component
class UserServiceBrokerOutcomeAdapter(brokerApi: BrokerApi, brokerProperties: BrokerProperties) {

    private val tradingAccountName = brokerProperties.tradingAccountName

    private val userService = brokerApi.userService

    fun getTradingAccountId(): String {
        return userService.accountsSync
            .first { it.name == tradingAccountName }
            .id
    }

}