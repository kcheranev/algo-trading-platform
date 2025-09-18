package com.github.trading.core.port.income.subscription

import com.github.trading.domain.model.subscription.CandleSubscription

interface SearchCandleSubscriptionUseCase {

    fun findAllCandleSubscriptions(): Set<CandleSubscription>

}