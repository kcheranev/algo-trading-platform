package ru.kcheranev.trading.core.port.outcome.notification

sealed class NotificationCommand

data class SendNotificationCommand(
    val text: String
) : NotificationCommand()