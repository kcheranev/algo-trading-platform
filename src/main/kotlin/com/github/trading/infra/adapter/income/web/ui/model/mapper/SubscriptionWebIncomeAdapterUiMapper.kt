package com.github.trading.infra.adapter.income.web.ui.model.mapper

import com.github.trading.domain.model.subscription.CandleSubscription
import com.github.trading.infra.adapter.income.web.ui.model.response.CandleSubscriptionUiDto
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
interface SubscriptionWebIncomeAdapterUiMapper {

    fun map(source: CandleSubscription): CandleSubscriptionUiDto

}

val subscriptionWebIncomeAdapterUiMapper: SubscriptionWebIncomeAdapterUiMapper = Mappers.getMapper(
    SubscriptionWebIncomeAdapterUiMapper::class.java
)