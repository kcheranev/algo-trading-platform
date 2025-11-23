package com.github.trading.infra.adapter.income.web.ui.impl

import com.github.trading.core.port.income.subscription.SearchCandleSubscriptionUseCase
import com.github.trading.infra.adapter.income.web.ui.model.mapper.subscriptionWebIncomeAdapterUiMapper
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("ui/subscriptions")
class SubscriptionUiController(
    private val searchCandleSubscriptionUseCase: SearchCandleSubscriptionUseCase
) {

    @GetMapping
    fun findAllCandleSubscriptions(model: Model): String {
        val candleSubscriptionDtoList =
            searchCandleSubscriptionUseCase.findAllCandleSubscriptions()
                .map(subscriptionWebIncomeAdapterUiMapper::map)
                .sortedBy { candleSubscription -> candleSubscription.instrument.ticker }
        model.addAttribute("candleSubscriptions", candleSubscriptionDtoList)
        return "subscriptions"
    }

}