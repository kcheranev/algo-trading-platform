package ru.kcheranev.trading.test.integration

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import ru.kcheranev.trading.core.port.common.model.Page
import ru.kcheranev.trading.core.port.common.model.Sort
import ru.kcheranev.trading.core.port.common.model.SortDirection
import ru.kcheranev.trading.domain.entity.StrategyConfigurationSort
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyConfigurationSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyConfigurationSearchResponse
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.extension.CleanDatabaseExtension

@IntegrationTest
class SearchStrategyConfigurationIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val jdbcTemplate: JdbcTemplate
) : StringSpec({

    extension(CleanDatabaseExtension(jdbcTemplate))

    beforeEach {
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
    }

    "should search strategy configuration by type" {
        //given
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
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val strategyConfigurationsResult = response.body!!.strategyConfigurations
        strategyConfigurationsResult.size shouldBe 1
        strategyConfigurationsResult[0].type shouldBe StrategyType.MOVING_MOMENTUM.name
    }

    "should search strategy configuration by candleInterval" {
        //given
        val request =
            StrategyConfigurationSearchRequest(
                candleInterval = CandleInterval.ONE_MIN
            )

        //when
        val response = testRestTemplate.postForEntity(
            "/strategy-configurations/search",
            request,
            StrategyConfigurationSearchResponse::class.java
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
            StrategyConfigurationSearchRequest(
                page = Page(1, 1),
                sort = Sort(StrategyConfigurationSort.TYPE, SortDirection.DESC)
            )

        //when
        val response = testRestTemplate.postForEntity(
            "/strategy-configurations/search",
            request,
            StrategyConfigurationSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val strategyConfigurationsResult = response.body!!.strategyConfigurations
        strategyConfigurationsResult.size shouldBe 1
        strategyConfigurationsResult[0].type shouldBe "TEST_3"
    }

    "should return empty result when there are no strategy configurations found" {
        //given
        val request =
            StrategyConfigurationSearchRequest(
                type = "any other type"
            )

        //when
        val response = testRestTemplate.postForEntity(
            "/strategy-configurations/search",
            request,
            StrategyConfigurationSearchResponse::class.java
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