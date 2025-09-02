package ru.kcheranev.trading.core.error

sealed interface Error {

    val message: String

}

sealed interface DomainError : Error

sealed interface IntegrationError : Error

sealed interface BrokerIntegrationError : IntegrationError

data object BestPriceBuyOrderExecutionError : BrokerIntegrationError {

    override val message = "An error has been occurred while executing best price buy order"

}

data object BestPriceSellOrderExecutionError : BrokerIntegrationError {

    override val message = "An error has been occurred while executing best price sell order"

}

data object GetTradingAccountError : BrokerIntegrationError {

    override val message = "An error has been occurred while getting trading account"

}

data object GetWithdrawLimitsError : BrokerIntegrationError {

    override val message = "An error has been occurred while getting withdraw limits"

}

data object NotificationError : IntegrationError {

    override val message = "An error has been occurred while sending a notification"

}