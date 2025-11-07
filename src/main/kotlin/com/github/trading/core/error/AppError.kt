package com.github.trading.core.error

sealed interface AppError {

    val message: String

}

sealed interface DomainError : AppError {

    data object OrderLotsQuantityCalculatingError : DomainError {

        override val message = "An error has been occurred while calculating lots quantity"

    }

    data object NotEnoughMoneyOnDepositError : DomainError {

        override val message = "Not enough money on deposit"

    }

}

data class ValidationError(
    val errors: List<String> = emptyList(),
    val fieldErrors: Map<String, List<String>> = emptyMap()
) : AppError {

    override val message = "Validation error: ${errorMessage()}"

    fun errorMessage(): String {
        val messageBuilder = StringBuilder()
        if (errors.isNotEmpty()) {
            messageBuilder.append(errors.joinToString(", "))
        }
        if (fieldErrors.isNotEmpty()) {
            fieldErrors.forEach { key, value ->
                if (messageBuilder.isNotEmpty()) {
                    messageBuilder.append(". ")
                }
                messageBuilder.append("$key: ${value.joinToString(", ")}")
            }
        }
        return messageBuilder.toString()
    }

}

sealed interface IntegrationError : AppError {

    sealed interface BrokerIntegrationError : IntegrationError {

        data object BestPriceBuyOrderExecutionError : BrokerIntegrationError {

            override val message = "An error has been occurred while executing best price buy order"

        }

        data object BestPriceSellOrderExecutionError : BrokerIntegrationError {

            override val message = "An error has been occurred while executing best price sell order"

        }

        data object GetMaxLotsError : BrokerIntegrationError {

            override val message = "An error has been occurred while getting max lots"

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

    }

    data object NotificationError : IntegrationError {

        override val message = "An error has been occurred while sending a notification"

    }

}