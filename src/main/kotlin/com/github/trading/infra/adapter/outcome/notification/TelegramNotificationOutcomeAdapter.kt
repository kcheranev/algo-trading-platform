package com.github.trading.infra.adapter.outcome.notification

import arrow.core.Either
import arrow.core.Either.Companion.catch
import com.github.trading.core.error.NotificationError
import com.github.trading.core.port.outcome.notification.NotificationPort
import com.github.trading.core.port.outcome.notification.SendNotificationCommand
import com.github.trading.infra.config.properties.TelegramNotificationProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class TelegramNotificationOutcomeAdapter(
    telegramNotificationProperties: TelegramNotificationProperties,
    private val restTemplate: RestTemplate
) : NotificationPort {

    private val enabled = telegramNotificationProperties.enabled

    private val apiUrl = telegramNotificationProperties.apiUrl

    private val chatId = telegramNotificationProperties.chatId

    override fun sendNotification(command: SendNotificationCommand): Either<NotificationError, Unit> =
        catch {
            if (enabled) {
                restTemplate.postForLocation(
                    "$apiUrl/sendMessage?chat_id=${chatId}&text=${command.text}",
                    Unit
                )
            }
        }.mapLeft { NotificationError }

}