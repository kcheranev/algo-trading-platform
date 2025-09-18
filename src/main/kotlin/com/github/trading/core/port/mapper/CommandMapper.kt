package com.github.trading.core.port.mapper

import com.github.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationCommand
import com.github.trading.core.port.income.tradeorder.SearchTradeOrderCommand
import com.github.trading.core.port.income.tradesession.SearchTradeSessionCommand
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
interface CommandMapper {

    fun map(command: SearchTradeSessionCommand): com.github.trading.core.port.outcome.persistence.tradesession.SearchTradeSessionCommand

    fun map(command: SearchStrategyConfigurationCommand): com.github.trading.core.port.outcome.persistence.strategyconfiguration.SearchStrategyConfigurationCommand

    fun map(command: SearchTradeOrderCommand): com.github.trading.core.port.outcome.persistence.tradeorder.SearchTradeOrderCommand

}

val commandMapper: CommandMapper = Mappers.getMapper(CommandMapper::class.java)