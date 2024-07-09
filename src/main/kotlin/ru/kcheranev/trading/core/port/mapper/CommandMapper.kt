package ru.kcheranev.trading.core.port.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.income.tradeorder.SearchTradeOrderCommand
import ru.kcheranev.trading.core.port.income.tradesession.SearchTradeSessionCommand

@Mapper
interface CommandMapper {

    fun map(command: SearchTradeSessionCommand): ru.kcheranev.trading.core.port.outcome.persistence.tradesession.SearchTradeSessionCommand

    fun map(command: SearchStrategyConfigurationCommand): ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.SearchStrategyConfigurationCommand

    fun map(command: SearchTradeOrderCommand): ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.SearchTradeOrderCommand

}

val commandMapper: CommandMapper = Mappers.getMapper(CommandMapper::class.java)