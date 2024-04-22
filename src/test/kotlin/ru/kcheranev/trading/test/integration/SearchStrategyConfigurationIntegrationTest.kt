package ru.kcheranev.trading.test.integration

import io.kotest.assertions.withClue
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.core.port.common.model.Page
import ru.kcheranev.trading.core.port.common.model.sort.Sort
import ru.kcheranev.trading.core.port.common.model.sort.SortDirection
import ru.kcheranev.trading.core.port.common.model.sort.StrategyConfigurationSort
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyConfigurationSearchRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyConfigurationSearchResponseDto
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.test.IntegrationTest

@IntegrationTest
class SearchStrategyConfigurationIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    beforeEach {
        val strategyConfigurations =
            listOf(
                StrategyConfigurationEntity(
                    null,
                    StrategyType.MOVING_MOMENTUM.name,
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param1" to 1))
                ),
                StrategyConfigurationEntity(
                    null,
                    "TEST_1",
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param2" to 2))
                ),
                StrategyConfigurationEntity(
                    null,
                    "TEST_2",
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param3" to 3))
                ),
                StrategyConfigurationEntity(
                    null,
                    "TEST_3",
                    CandleInterval.FIVE_MIN,
                    MapWrapper(mapOf("param4" to 4))
                ),
                StrategyConfigurationEntity(
                    null,
                    "TEST_4",
                    CandleInterval.FIVE_MIN,
                    MapWrapper(mapOf("param5" to 5))
                )
            )
        strategyConfigurationRepository.saveAll(strategyConfigurations)
    }

    "should search strategy configuration by type" {
        //given
        val request =
            StrategyConfigurationSearchRequestDto(
                type = StrategyType.MOVING_MOMENTUM.name
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
        strategyConfigurationsResult[0].type shouldBe StrategyType.MOVING_MOMENTUM.name
    }

    "should search strategy configurations" {
        //when
        val response = testRestTemplate.postForEntity(
            "/strategy-configurations/search",
            StrategyConfigurationSearchRequestDto(),
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
            StrategyConfigurationSearchRequestDto(
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
            StrategyConfigurationSearchRequestDto(
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
            StrategyConfigurationSearchRequestDto(
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