package ru.kcheranev.trading.test.integration

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyConfigurationSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyConfigurationSearchResponse
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.test.IntegrationTest

@IntegrationTest
class SearchStrategyConfigurationIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val strategyConfigurationRepository: StrategyConfigurationRepository
) : StringSpec({

    "should search strategy configuration" {
        //given
        val strategyConfigurations =
            listOf(
                StrategyConfigurationEntity(
                    null,
                    StrategyType.MOVING_MOMENTUM.name,
                    10,
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param1" to "value1"))
                ),
                StrategyConfigurationEntity(
                    null,
                    "TEST_1",
                    11,
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param12" to "value2"))
                ),
                StrategyConfigurationEntity(
                    null,
                    "TEST_2",
                    12,
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param3" to "value3"))
                ),
                StrategyConfigurationEntity(
                    null,
                    "TEST_3",
                    13,
                    CandleInterval.FIVE_MIN,
                    MapWrapper(mapOf("param4" to "value4"))
                ),
                StrategyConfigurationEntity(
                    null,
                    "TEST_4",
                    14,
                    CandleInterval.FIVE_MIN,
                    MapWrapper(mapOf("param5" to "value5"))
                )
            )
        strategyConfigurationRepository.saveAll(strategyConfigurations)

        val request =
            StrategyConfigurationSearchRequest(
                type = StrategyType.MOVING_MOMENTUM.name
            )

        //when
        val response = testRestTemplate.postForEntity(
            "/strategy-configurations/search",
            request,
            StrategyConfigurationSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
    }

})
