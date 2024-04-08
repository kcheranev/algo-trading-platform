package ru.kcheranev.trading.infra.adapter.income.web.impl

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchUseCase
import ru.kcheranev.trading.core.port.income.trading.StartTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.trading.StopTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.StopTradeSessionUseCase
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StartTradeSessionRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeSessionSearchRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StartTradeSessionResponseDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeSessionSearchResponseDto
import ru.kcheranev.trading.infra.adapter.income.web.webIncomeAdapterMapper
import java.util.UUID

@Tag(name = "Trade session", description = "Trade session operations")
@RestController
@RequestMapping("trade-sessions")
class TradeSessionController(
    private val startTradeSessionUseCase: StartTradeSessionUseCase,
    private val stopTradeSessionUseCase: StopTradeSessionUseCase,
    private val tradeSessionSearchUseCase: TradeSessionSearchUseCase
) {

    @Operation(summary = "Start trade session")
    @PostMapping
    fun start(@RequestBody request: StartTradeSessionRequestDto) =
        StartTradeSessionResponseDto(
            startTradeSessionUseCase.startTradeSession(webIncomeAdapterMapper.map(request)).value
        )

    @Operation(summary = "Stop trade session")
    @PostMapping("{id}/stop")
    fun stop(@Parameter(description = "Trade session id") @PathVariable id: UUID) =
        stopTradeSessionUseCase.stopTradeSession(StopTradeSessionCommand(TradeSessionId(id)))

    @Operation(summary = "Search trade sessions")
    @PostMapping("search")
    fun search(@RequestBody request: TradeSessionSearchRequestDto) =
        TradeSessionSearchResponseDto(
            tradeSessionSearchUseCase.search(webIncomeAdapterMapper.map(request))
                .map { webIncomeAdapterMapper.map(it) }
        )

}