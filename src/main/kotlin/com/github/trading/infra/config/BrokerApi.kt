package com.github.trading.infra.config

import io.grpc.Channel
import ru.tinkoff.piapi.core.InvestApi

class BrokerApi(val investApi: InvestApi) {

    fun destroy() {
        investApi.destroy(5)
    }

    companion object {

        fun init(token: String, appName: String) = BrokerApi(InvestApi.create(token, appName))

        fun init(channel: Channel) = BrokerApi(InvestApi.create(channel))

    }

}