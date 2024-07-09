package ru.kcheranev.trading.infra.adapter.income.web.rest.impl

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.tradesession.SearchTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.StartTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.StopTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.StopTradeSessionUseCase
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.mapper.tradeSessionWebIncomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.SearchTradeSessionRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StartTradeSessionRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.StartTradeSessionResponseDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.TradeSessionSearchResponseDto
import java.util.UUID

@Tag(name = "Trade session", description = "Trade session operations")
@RestController
@RequestMapping("trade-sessions")
class TradeSessionController(
    private val startTradeSessionUseCase: StartTradeSessionUseCase,
    private val stopTradeSessionUseCase: StopTradeSessionUseCase,
    private val tradeSessionSearchUseCase: SearchTradeSessionUseCase
) {

    @Operation(summary = "Start trade session")
    @PostMapping
    fun start(@RequestBody request: StartTradeSessionRequestDto) =
        StartTradeSessionResponseDto(
            startTradeSessionUseCase.startTradeSession(tradeSessionWebIncomeAdapterMapper.map(request)).value
        )

    @Operation(summary = "Stop trade session")
    @PostMapping("{id}/stop")
    fun stop(@Parameter(description = "Trade session id") @PathVariable id: UUID) =
        stopTradeSessionUseCase.stopTradeSession(StopTradeSessionCommand(TradeSessionId(id)))

    @Operation(summary = "Search trade sessions")
    @PostMapping("search")
    fun search(@RequestBody request: SearchTradeSessionRequestDto) =
        TradeSessionSearchResponseDto(
            tradeSessionSearchUseCase.search(tradeSessionWebIncomeAdapterMapper.map(request))
                .map { tradeSessionWebIncomeAdapterMapper.map(it) }
        )

}