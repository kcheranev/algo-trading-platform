package com.github.trading.test.integration

import com.github.trading.core.port.model.Page
import com.github.trading.core.port.model.sort.Sort
import com.github.trading.core.port.model.sort.SortDirection
import com.github.trading.core.port.model.sort.StrategyConfigurationSort
import com.github.trading.domain.model.CandleInterval
import com.github.trading.infra.adapter.income.web.rest.model.request.SearchStrategyConfigurationRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.response.StrategyConfigurationSearchResponseDto
import com.github.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper
import com.github.trading.test.IntegrationTest
import io.kotest.assertions.withClue
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.http.HttpStatus
import java.util.UUID

@IntegrationTest
class SearchStrategyConfigurationIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    beforeEach {
        val strategyConfigurations =
            listOf(
                StrategyConfigurationEntity(
                    id = UUID.randomUUID(),
                    name = "moving momentum",
                    type = "MOVING_MOMENTUM_LONG",
                    candleInterval = CandleInterval.ONE_MIN,
                    parameters = MapWrapper(mapOf("param1" to 1))
                ),
                StrategyConfigurationEntity(
                    id = UUID.randomUUID(),
                    name = "test1",
                    type = "TEST_1",
                    candleInterval = CandleInterval.ONE_MIN,
                    parameters = MapWrapper(mapOf("param2" to 2))
                ),
                StrategyConfigurationEntity(
                    id = UUID.randomUUID(),
                    name = "test2",
                    type = "TEST_2",
                    candleInterval = CandleInterval.ONE_MIN,
                    parameters = MapWrapper(mapOf("param3" to 3))
                ),
                StrategyConfigurationEntity(
                    id = UUID.randomUUID(),
                    name = "test3",
                    type = "TEST_3",
                    candleInterval = CandleInterval.FIVE_MIN,
                    parameters = MapWrapper(mapOf("param4" to 4))
                ),
                StrategyConfigurationEntity(
                    id = UUID.randomUUID(),
                    name = "test4",
                    type = "TEST_4",
                    candleInterval = CandleInterval.FIVE_MIN,
                    parameters = MapWrapper(mapOf("param5" to 5))
                )
            )
        jdbcTemplate.insertAll(strategyConfigurations)
    }

    "should search strategy configuration by type" {
        //given
        val request =
            SearchStrategyConfigurationRequestDto(
                type = "MOVING_MOMENTUM_LONG"
            )

        //when
        val response = testRestTemplate.postForEntity(
            "/strategy-configurations/search",
            request,
            StrategyConfigurationSearchResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val strategyConfigurationsResult = response.body!!.strategyConfigurations
        strategyConfigurationsResult.size shouldBe 1
        strategyConfigurationsResult[0].type shouldBe "MOVING_MOMENTUM_LONG"
    }

    "should search strategy configurations" {
        //when
        val response = testRestTemplate.postForEntity(
            "/strategy-configurations/search",
            SearchStrategyConfigurationRequestDto(),
            StrategyConfigurationSearchResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val strategyConfigurationsResult = response.body!!.strategyConfigurations
        strategyConfigurationsResult.size shouldBe 5
    }

    "should search strategy configuration by candleInterval" {
        //given
        val request =
            SearchStrategyConfigurationRequestDto(
                candleInterval = CandleInterval.ONE_MIN
            )

        //when
        val response = testRestTemplate.postForEntity(
            "/strategy-configurations/search",
            request,
            StrategyConfigurationSearchResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val strategyConfigurationsResult = response.body!!.strategyConfigurations
        strategyConfigurationsResult.size shouldBe 3
        strategyConfigurationsResult.forEach {
            it.candleInterval shouldBe CandleInterval.ONE_MIN
        }
    }

    "should search strategy configuration with paging and sorting" {
        //given
        val request =
            SearchStrategyConfigurationRequestDto(
                page = Page(2, 1),
                sort = Sort(StrategyConfigurationSort.TYPE, SortDirection.DESC)
            )

        //when
        val response = testRestTemplate.postForEntity(
            "/strategy-configurations/search",
            request,
            StrategyConfigurationSearchResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val strategyConfigurationsResult = response.body!!.strategyConfigurations
        strategyConfigurationsResult.size shouldBe 2
        strategyConfigurationsResult[0].type shouldBe "TEST_3"
        strategyConfigurationsResult[1].type shouldBe "TEST_2"
    }

    "should return empty result when there are no strategy configurations found" {
        //given
        val request =
            SearchStrategyConfigurationRequestDto(
                type = "any other type"
            )

        //when
        val response = testRestTemplate.postForEntity(
            "/strategy-configurations/search",
            request,
            StrategyConfigurationSearchResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val strategyConfigurationsResult = response.body!!.strategyConfigurations
        strategyConfigurationsResult.size shouldBe 0
    }

})