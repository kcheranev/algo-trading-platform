package com.github.trading.core.port.outcome.notification

import arrow.core.Either
import com.github.trading.core.error.NotificationError

interface NotificationPort {

    fun sendNotification(command: SendNotificationCommand): Either<NotificationError, Unit>

}