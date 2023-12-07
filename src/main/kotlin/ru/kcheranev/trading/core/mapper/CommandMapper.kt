package ru.kcheranev.trading.core.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.search.OrderSearchCommand
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchCommand
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchCommand

@Mapper
interface CommandMapper {

    fun map(command: TradeSessionSearchCommand): ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionSearchCommand

    fun map(command: StrategyConfigurationSearchCommand): ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationSearchCommand

    fun map(command: OrderSearchCommand): ru.kcheranev.trading.core.port.outcome.persistence.OrderSearchCommand

}

val commandMapper: CommandMapper = Mappers.getMapper(CommandMapper::class.java)