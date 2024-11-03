package ru.kcheranev.trading.infra.adapter.income.broker

import ru.kcheranev.trading.domain.exception.InfrastructureException
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

class UnexpectedSubscriptionIntervalException(subscriptionInterval: SubscriptionInterval) :
    InfrastructureException("Unexpected subscription interval $subscriptionInterval")