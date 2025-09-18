package com.github.trading.infra.adapter.income.broker

import com.github.trading.domain.exception.InfrastructureException
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

class UnexpectedSubscriptionIntervalException(subscriptionInterval: SubscriptionInterval) :
    InfrastructureException("Unexpected subscription interval $subscriptionInterval")