package ru.kcheranev.trading.infra.config

import ru.tinkoff.piapi.core.InvestApi

class BrokerApi(token: String) {

    private val investApi: InvestApi = InvestApi.create(token)

    fun destroy() {
        investApi.destroy(5)
    }

    fun orderService() = investApi.ordersService

    fun userService() = investApi.userService

}