package ru.kcheranev.trading.infra.adapter.income.broker

import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

open class BrokerIncomeAdapterException(
    message: String
) : RuntimeException(message)

class UnexpectedSubscriptionIntervalException(subscriptionInterval: SubscriptionInterval) :
    BrokerIncomeAdapterException("Unexpected subscription interval $subscriptionInterval")