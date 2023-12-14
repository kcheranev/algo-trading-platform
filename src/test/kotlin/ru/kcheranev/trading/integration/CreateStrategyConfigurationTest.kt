package ru.kcheranev.trading.integration

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import ru.kcheranev.trading.IntegrationTest
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import ru.kcheranev.trading.infra.adapter.income.web.model.request.CreateStrategyConfigurationRequest
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository

@IntegrationTest
class CreateStrategyConfigurationTest(
    private val testRestTemplate: TestRestTemplate,
    private val strategyConfigurationRepository: StrategyConfigurationRepository
) : StringSpec({

    "should create strategy configuration" {
        //given
        val request =
            CreateStrategyConfigurationRequest(
                type = StrategyType.MOVING_MOMENTUM,
                initCandleAmount = 10,
                candleInterval = CandleInterval.ONE_MIN,
                params = mapOf("paramKey" to "paramValue")
            )

        //when
        val response = testRestTemplate.postForEntity("/strategy-configurations", request, Unit::class.java)
        println(response)

        //then
        strategyConfigurationRepository.findAll().count() shouldBe 1
    }

})