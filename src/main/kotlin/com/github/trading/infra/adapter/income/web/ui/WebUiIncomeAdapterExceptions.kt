package com.github.trading.infra.adapter.income.web.ui

import com.github.trading.domain.exception.InfrastructureException

class NotFoundException(message: String) : InfrastructureException(message)