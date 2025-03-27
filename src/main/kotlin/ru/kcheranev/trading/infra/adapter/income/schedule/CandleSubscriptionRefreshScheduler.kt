package ru.kcheranev.trading.infra.adapter.income.schedule

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.port.income.subscription.RefreshCandleSubscriptionsUseCase

@Component
@ConditionalOnProperty("application.schedule.enabled")
class CandleSubscriptionRefreshScheduler(
    private val refreshCandleSubscriptionsUseCase: RefreshCandleSubscriptionsUseCase
) {

    @Scheduled(fixedDelayString = "\${application.schedule.candle-subscription-refresh-delay}")
    fun refreshCandleSubscriptions() {
        refreshCandleSubscriptionsUseCase.refreshCandleSubscriptions()
    }

}