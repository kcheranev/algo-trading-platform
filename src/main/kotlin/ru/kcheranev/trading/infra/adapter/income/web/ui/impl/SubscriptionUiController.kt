package ru.kcheranev.trading.infra.adapter.income.web.ui.impl

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.kcheranev.trading.core.port.income.subscription.SearchSubscriptionUseCase
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.subscriptionWebIncomeAdapterUiMapper

@Controller
@RequestMapping("ui/subscriptions")
class SubscriptionUiController(
    private val searchSubscriptionUseCase: SearchSubscriptionUseCase
) {

    @GetMapping
    fun findAllCandleSubscriptions(model: Model): String {
        val candleSubscriptionDtoList =
            searchSubscriptionUseCase.findAllCandleSubscriptions()
                .map { subscriptionWebIncomeAdapterUiMapper.map(it) }
        model.addAttribute("candleSubscriptions", candleSubscriptionDtoList)
        return "subscriptions"
    }

}