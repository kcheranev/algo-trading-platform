package ru.kcheranev.trading.infra.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("application.infra.notification.telegram")
class TelegramNotificationProperties @ConstructorBinding constructor(
    val apiUrl: String,
    val chatId: String
)