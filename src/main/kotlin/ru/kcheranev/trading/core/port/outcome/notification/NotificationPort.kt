package ru.kcheranev.trading.core.port.outcome.notification

import arrow.core.Either
import ru.kcheranev.trading.core.error.NotificationError

interface NotificationPort {

    fun sendNotification(command: SendNotificationCommand): Either<NotificationError, Unit>

}