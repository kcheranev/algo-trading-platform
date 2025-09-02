package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.raise.either
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.kcheranev.trading.common.defaultCurrency
import ru.kcheranev.trading.core.error.BrokerIntegrationError
import ru.kcheranev.trading.core.error.GetWithdrawLimitsError
import ru.kcheranev.trading.core.port.outcome.broker.WithdrawLimitsBrokerPort
import ru.tinkoff.piapi.core.OperationsService
import java.math.BigDecimal

@Component
class WithdrawLimitsBrokerOutcomeAdapter(
    private val userServiceBrokerOutcomeAdapter: UserServiceBrokerOutcomeAdapter,
    private val operationsService: OperationsService
) : WithdrawLimitsBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getWithdrawLimits(): Either<BrokerIntegrationError, BigDecimal> =
        either {
            val accountId = userServiceBrokerOutcomeAdapter.getTradingAccountId().bind()
            val withdrawLimits =
                catch { operationsService.getWithdrawLimitsSync(accountId) }
                    .onLeft { ex -> log.error("An error has been occurred while getting withdraw limits", ex) }
                    .mapLeft { GetWithdrawLimitsError }
                    .bind()
            withdrawLimits.money
                .firstOrNull { money -> money.currency.equals(defaultCurrency, true) }
                ?.value
                ?: BigDecimal.ZERO
        }

}