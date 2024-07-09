package ru.kcheranev.trading.core.port.income.subscription

import ru.kcheranev.trading.domain.model.subscription.CandleSubscription

interface SearchSubscriptionUseCase {

    fun findAllCandleSubscriptions(): List<CandleSubscription>

}