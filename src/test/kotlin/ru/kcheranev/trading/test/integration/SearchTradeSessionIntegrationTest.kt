package ru.kcheranev.trading.test.integration

import io.kotest.assertions.withClue
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeSessionSearchRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeSessionSearchResponseDto
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeSessionCache
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.test.IntegrationTest
import java.time.LocalDateTime
import java.util.UUID

@IntegrationTest
class SearchTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val tradeSessionCache: TradeSessionCache,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    beforeEach {
        val strategyConfiguration =
            strategyConfigurationRepository.save(
                StrategyConfigurationEntity(
                    null,
                    StrategyType.MOVING_MOMENTUM.name,
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param1" to 1))
                )
            )
        val tradeSession1Id = UUID.randomUUID()
        tradeSessionCache.put(
            tradeSession1Id,
            TradeSession(
                id = TradeSessionId(tradeSession1Id),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.parse("2024-01-01T10:15:30"),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = mockk(),
                strategyConfigurationId = StrategyConfigurationId(strategyConfiguration.id!!)
            )
        )
        val tradeSession2Id = UUID.randomUUID()
        tradeSessionCache.put(
            tradeSession2Id,
            TradeSession(
                id = TradeSessionId(tradeSession2Id),
                ticker = "MOEX",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b2",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.parse("2024-01-01T10:15:30"),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = mockk(),
                strategyConfigurationId = StrategyConfigurationId(strategyConfiguration.id!!)
            )
        )
    }

    "should search trade sessions" {
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-sessions/search",
            TradeSessionSearchRequestDto(),
            TradeSessionSearchResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeSessionsResult = response.body!!.tradeSessions
        tradeSessionsResult.size shouldBe 2
    }

})