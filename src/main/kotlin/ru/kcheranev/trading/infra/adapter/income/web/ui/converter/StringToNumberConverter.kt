package ru.kcheranev.trading.infra.adapter.income.web.ui.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToNumberConverter : Converter<String, Number> {

    override fun convert(source: String): Number? {
        if (source.isEmpty()) {
            return null
        }
        if (source.contains(".")) {
            return source.toBigDecimal()
        }
        return source.toInt()
    }

}