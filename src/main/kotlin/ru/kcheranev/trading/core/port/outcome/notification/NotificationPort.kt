package ru.kcheranev.trading.core.port.outcome.notification

interface NotificationPort {

    fun sendNotification(command: SendNotificationCommand)

}