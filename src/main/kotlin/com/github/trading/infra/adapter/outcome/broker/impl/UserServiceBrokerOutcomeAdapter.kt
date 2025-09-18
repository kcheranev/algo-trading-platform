package com.github.trading.infra.adapter.outcome.broker.impl

import arrow.core.Either
import arrow.core.Either.Companion.catch
import com.github.trading.common.getOrPut
import com.github.trading.core.error.BrokerIntegrationError
import com.github.trading.core.error.GetTradingAccountError
import com.github.trading.core.port.outcome.broker.UserServiceBrokerPort
import com.github.trading.domain.exception.InfrastructureException
import com.github.trading.infra.config.properties.BrokerProperties
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import ru.tinkoff.piapi.core.UsersService

private const val ACCOUNT_ID_CACHE = "accountId"

@Component
class UserServiceBrokerOutcomeAdapter(
    private val brokerUsersService: UsersService,
    brokerProperties: BrokerProperties,
    cacheManager: CacheManager
) : UserServiceBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val tradingAccountName = brokerProperties.tradingAccountName

    private val accountIdCache =
        cacheManager.getCache(ACCOUNT_ID_CACHE)
            ?: throw InfrastructureException("There is no $ACCOUNT_ID_CACHE")

    override fun getTradingAccountId(): Either<BrokerIntegrationError, String> =
        catch {
            accountIdCache.getOrPut("accountId") {
                brokerUsersService.accountsSync
                    .first { it.name == tradingAccountName }
                    .id
            }
        }.onLeft { ex -> log.error("An error has been occurred while getting trading account", ex) }
            .mapLeft { GetTradingAccountError }

}