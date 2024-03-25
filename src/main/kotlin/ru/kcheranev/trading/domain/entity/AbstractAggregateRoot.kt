package ru.kcheranev.trading.domain.entity

import ru.kcheranev.trading.domain.DomainEvent

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