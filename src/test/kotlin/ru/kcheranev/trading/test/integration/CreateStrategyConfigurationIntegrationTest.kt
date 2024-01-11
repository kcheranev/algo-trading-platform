package ru.kcheranev.trading.test.integration

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import ru.kcheranev.trading.infra.adapter.income.web.model.request.CreateStrategyConfigurationRequest
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.extension.CleanDatabaseExtension

@IntegrationTest
class CreateStrategyConfigurationIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val jdbcTemplate: JdbcTemplate
) : StringSpec({

    extension(CleanDatabaseExtension(jdbcTemplate))

    "should create strategy configuration" {
        //given
        val request =
            CreateStrategyConfigurationRequest(
                type = StrategyType.MOVING_MOMENTUM.name,
                initCandleAmount = 10,
                candleInterval = CandleInterval.ONE_MIN,
                params = mapOf("paramKey" to "paramValue")
            )

        //when
        val response = testRestTemplate.postForEntity("/strategy-configurations", request, Unit::class.java)

        //then
        response.statusCode shouldBe HttpStatus.OK

        val persistenceResult = strategyConfigurationRepository.findAll().toList()
        persistenceResult shouldHaveSize 1

        val strategyConfiguration = persistenceResult.first()
        strategyConfiguration.type shouldBe StrategyType.MOVING_MOMENTUM.name
        strategyConfiguration.initCandleAmount shouldBe 10
        strategyConfiguration.candleInterval shouldBe CandleInterval.ONE_MIN
        strategyConfiguration.params.value shouldBe mapOf("paramKey" to "paramValue")
    }

})