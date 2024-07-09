package ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.domain.model.subscription.CandleSubscription
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.CandleSubscriptionUiDto

@Mapper
interface SubscriptionWebIncomeAdapterUiMapper {

    fun map(source: CandleSubscription): CandleSubscriptionUiDto

}

val subscriptionWebIncomeAdapterUiMapper: SubscriptionWebIncomeAdapterUiMapper = Mappers.getMapper(
    SubscriptionWebIncomeAdapterUiMapper::class.java
)