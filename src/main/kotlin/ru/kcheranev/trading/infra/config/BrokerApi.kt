package ru.kcheranev.trading.infra.config

import io.grpc.Channel
import ru.tinkoff.piapi.core.InvestApi

class BrokerApi(private val investApi: InvestApi) {

    val orderService = investApi.ordersService

    val userService = investApi.userService

    val marketDataStreamService = investApi.marketDataStreamService

    val marketDataService = investApi.marketDataService

    fun destroy() {
        investApi.destroy(5)
    }

    companion object {

        fun init(token: String, appName: String) = BrokerApi(InvestApi.create(token, appName))

        fun init(channel: Channel) = BrokerApi(InvestApi.create(channel))

    }

}