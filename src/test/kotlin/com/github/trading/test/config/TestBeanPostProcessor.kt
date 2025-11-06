package com.github.trading.test.config

import io.mockk.mockk
import org.springframework.beans.factory.config.BeanPostProcessor
import ru.ttech.piapi.core.impl.marketdata.MarketDataStreamManager

class TestBeanPostProcessor : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        return when (bean) {
            is MarketDataStreamManager -> mockk<MarketDataStreamManager>(relaxed = true)

            else -> bean
        }
    }

}