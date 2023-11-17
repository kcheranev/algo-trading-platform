package ru.kcheranev.trading.domain

abstract class AbstractEntity(
    val events: MutableSet<DomainEvent> = mutableSetOf()
) {

    fun registerEvent(domainEvent: DomainEvent) {
        events.add(domainEvent)
    }

}