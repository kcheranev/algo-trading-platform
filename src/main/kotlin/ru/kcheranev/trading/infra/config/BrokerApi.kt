package ru.kcheranev.trading.infra.config

import ru.tinkoff.piapi.core.InvestApi

class BrokerApi(token: String) {

    private val investApi: InvestApi = InvestApi.create(token)

    val orderService = investApi.ordersService

    val userService = investApi.userService

    val marketDataStreamService = investApi.marketDataStreamService

    val marketDataService = investApi.marketDataService

    fun destroy() {
        investApi.destroy(5)
    }

}