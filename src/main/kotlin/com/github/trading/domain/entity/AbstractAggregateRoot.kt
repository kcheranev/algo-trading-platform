package com.github.trading.domain.entity

import com.github.trading.domain.DomainEvent

abstract class AbstractAggregateRoot(
    val events: MutableList<DomainEvent> = mutableListOf()
) {

    fun registerEvent(domainEvent: DomainEvent) {
        events.add(domainEvent)
    }

    fun clearEvents() {
        events.clear()
    }

}