package com.github.trading.test.stub.http

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.trading.infra.config.properties.TelegramNotificationProperties
import com.github.trading.test.stub.WireMockServers.httpWireMockServer

class TelegramNotificationHttpStub(
    private val notificationProperties: TelegramNotificationProperties
) {

    fun stubForSendNotification() {
        httpWireMockServer.stubFor(
            post(urlPathMatching("/sendMessage"))
                .withQueryParam("chat_id", equalTo(notificationProperties.chatId))
                .willReturn(aResponse().withStatus(200))
        )
    }

}