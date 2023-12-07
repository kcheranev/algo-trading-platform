package ru.kcheranev.trading.infra.adapter.income.web

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.search.OrderSearchCommand
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchCommand
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchCommand
import ru.kcheranev.trading.domain.entity.Order
import ru.kcheranev.trading.domain.entity.OrderId
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.infra.adapter.income.web.model.request.OrderSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyConfigurationSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeSessionSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.OrderDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyConfigurationDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeSessionDto

@Mapper
interface WebIncomeAdapterMapper {

    fun map(source: TradeSessionSearchRequest): TradeSessionSearchCommand

    fun map(source: StrategyConfigurationSearchRequest): StrategyConfigurationSearchCommand

    fun map(source: OrderSearchRequest): OrderSearchCommand

    fun mapLongToTradeSessionId(source: Long): TradeSessionId = TradeSessionId(source)

    fun mapLongToStrategyConfigurationId(source: Long): StrategyConfigurationId = StrategyConfigurationId(source)

    fun mapLongToOrderId(source: Long): OrderId = OrderId(source)

    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "strategyConfigurationId.value", target = "strategyConfigurationId")
    fun map(source: TradeSession): TradeSessionDto

    @Mapping(source = "id.value", target = "id")
    fun map(source: StrategyConfiguration): StrategyConfigurationDto

    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "tradeSessionId.value", target = "tradeSessionId")
    fun map(source: Order): OrderDto

}

val webIncomeAdapterMapper: WebIncomeAdapterMapper = Mappers.getMapper(WebIncomeAdapterMapper::class.java)