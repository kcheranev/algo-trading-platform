package ru.kcheranev.trading.infra.adapter.outcome.notification

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.kcheranev.trading.core.port.outcome.notification.NotificationPort
import ru.kcheranev.trading.core.port.outcome.notification.SendNotificationCommand
import ru.kcheranev.trading.infra.config.properties.TelegramNotificationProperties

@Component
class TelegramNotificationOutcomeAdapter(
    telegramNotificationProperties: TelegramNotificationProperties,
    private val restTemplate: RestTemplate
) : NotificationPort {

    private val apiUrl = telegramNotificationProperties.apiUrl

    private val chatId = telegramNotificationProperties.chatId

    override fun sendNotification(command: SendNotificationCommand) {
        restTemplate.postForLocation(
            "$apiUrl/sendMessage?chat_id=${chatId}&text=${command.text}",
            Unit
        )
    }

}