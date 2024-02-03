package ru.kcheranev.trading.test.integration

import io.kotest.assertions.withClue
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeSessionSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeSessionSearchResponse
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeSessionRepository
import ru.kcheranev.trading.test.IntegrationTest
import java.time.LocalDateTime

@IntegrationTest
class SearchTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val tradeSessionRepository: TradeSessionRepository,
    private val tradeStrategyCache: TradeStrategyCache,
    private val integrationTestExtensions: List<Extension>
) : StringSpec({

    extensions(integrationTestExtensions)

    beforeEach {
        val strategyConfiguration =
            strategyConfigurationRepository.save(
                StrategyConfigurationEntity(
                    null,
                    StrategyType.MOVING_MOMENTUM.name,
                    10,
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param1" to "value1"))
                )
            )
        val tradeSessions =
            tradeSessionRepository.saveAll(
                listOf(
                    TradeSessionEntity(
                        ticker = "SBER",
                        instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                        status = TradeSessionStatus.WAITING,
                        startDate = LocalDateTime.parse("2024-01-01T10:15:30"),
                        candleInterval = CandleInterval.ONE_MIN,
                        lotsQuantity = 10,
                        lastEventDate = LocalDateTime.parse("2024-01-01T10:16:10"),
                        strategyConfigurationId = strategyConfiguration.id!!
                    ),
                    TradeSessionEntity(
                        ticker = "MOEX",
                        instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b2",
                        status = TradeSessionStatus.WAITING,
                        startDate = LocalDateTime.parse("2024-01-01T10:15:30"),
                        candleInterval = CandleInterval.ONE_MIN,
                        lotsQuantity = 10,
                        lastEventDate = LocalDateTime.parse("2024-01-01T10:16:10"),
                        strategyConfigurationId = strategyConfiguration.id!!
                    )
                )
            )

        tradeSessions.forEach { tradeStrategyCache.put(it.id!!, mockk()) }
    }

    "should search trade sessions" {
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-sessions/search",
            TradeSessionSearchRequest(),
            TradeSessionSearchResponse::class.java
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