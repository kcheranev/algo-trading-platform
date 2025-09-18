package com.github.trading.test.integration

import com.github.trading.domain.model.CandleInterval
import com.github.trading.infra.adapter.income.web.rest.model.request.CreateStrategyConfigurationRequestDto
import com.github.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import com.github.trading.test.IntegrationTest
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.http.HttpStatus
import java.math.BigDecimal

@IntegrationTest
class CreateStrategyConfigurationIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    "should create strategy configuration" {
        //given
        val request =
            CreateStrategyConfigurationRequestDto(
                type = "MOVING_MOMENTUM_LONG",
                name = "moving momentum",
                candleInterval = CandleInterval.ONE_MIN,
                parameters = mapOf(
                    "paramKeyInt" to 1,
                    "paramKeyBigDecimal" to BigDecimal("2.25")
                )
            )

        //when
        val response = testRestTemplate.postForEntity("/strategy-configurations", request, Unit::class.java)

        //then
        response.statusCode shouldBe HttpStatus.OK

        val persistenceResult = jdbcTemplate.findAll(StrategyConfigurationEntity::class.java)
        persistenceResult shouldHaveSize 1

        val strategyConfiguration = persistenceResult.first()
        strategyConfiguration.type shouldBe "MOVING_MOMENTUM_LONG"
        strategyConfiguration.candleInterval shouldBe CandleInterval.ONE_MIN
        strategyConfiguration.parameters.value shouldBe mapOf(
            "paramKeyInt" to 1,
            "paramKeyBigDecimal" to BigDecimal("2.25")
        )
    }

})