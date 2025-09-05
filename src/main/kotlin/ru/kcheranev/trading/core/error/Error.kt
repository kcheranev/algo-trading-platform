package ru.kcheranev.trading.core.error

sealed interface Error {

    val message: String

}

sealed interface DomainError : Error

data object OrderLotsQuantityCalculatingError : DomainError {

    override val message = "An error has been occurred while calculating lots quantity"

}

data object NotEnoughMoneyOnDepositError : DomainError {

    override val message = "Not enough money on deposit"

}

data class ValidationError(private val errors: List<String>) : DomainError {

    override val message = "Validation error: ${errors.joinToString(", ")}"

}

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

data object GetPortfolioError : BrokerIntegrationError {

    override val message = "An error has been occurred while getting portfolio"

}

data object GetShareByIdError : BrokerIntegrationError {

    override val message = "An error has been occurred while getting share by id"

}

data object NotificationError : IntegrationError {

    override val message = "An error has been occurred while sending a notification"

}