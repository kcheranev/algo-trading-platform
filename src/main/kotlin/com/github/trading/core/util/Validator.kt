package com.github.trading.core.util

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.raise.either
import com.github.trading.core.error.ValidationError
import com.github.trading.domain.exception.ValidationException
import org.slf4j.LoggerFactory

class Validator {

    val errors: MutableList<String> = mutableListOf()

    val fieldErrors: MutableMap<String, MutableList<String>> = mutableMapOf()

    fun addError(error: String) {
        errors.add(error)
    }

    fun addFieldError(fieldName: String, error: String) {
        fieldErrors.compute(fieldName) { _, value ->
            value?.apply { add(error) } ?: mutableListOf(error)
        }
    }

    fun field(fieldName: String, doValidate: FieldValidator.() -> Unit) {
        val fieldValidator = FieldValidator(this, fieldName)
        fieldValidator.doValidate()
    }

    companion object {

        private val log = LoggerFactory.getLogger(Validator::class.java)

        fun validate(doValidate: Validator.() -> Unit): Either<ValidationError, Unit> =
            either {
                val validator = Validator()
                catch { validator.doValidate() }
                    .onLeft { ex -> log.error("An error has been occurred while validating", ex) }
                    .mapLeft { ValidationError(listOf("Validation failed")) }
                    .bind()
                if (validator.fieldErrors.isNotEmpty()) {
                    raise(ValidationError(validator.errors, validator.fieldErrors))
                }
            }.onLeft { validationError -> log.warn(validationError.message) }

        fun validateOrThrow(doValidate: Validator.() -> Unit) {
            validate(doValidate).onLeft { validationError -> throw ValidationException("Validation failed", validationError.errors) }
        }

    }

}

class FieldValidator(
    private val validator: Validator,
    private val fieldName: String
) {

    fun Any?.shouldNotBeNull(message: String? = null) {
        if (this == null) {
            validator.addFieldError(fieldName, message ?: "$fieldName must not be null")
        }
    }

    fun <T> Comparable<T>?.shouldBeGreaterThan(value: T, message: String? = null) {
        if (this != null && this <= value) {
            validator.addFieldError(fieldName, message ?: "$fieldName must be greater than $value")
        }
    }

    fun <T> Comparable<T>?.shouldBeLessThan(value: T, message: String? = null) {
        if (this != null && this >= value) {
            validator.addFieldError(fieldName, message ?: "$fieldName must be less than $value")
        }
    }

    fun <T> Comparable<T>?.shouldBeGreaterThanOrEquals(value: T, message: String? = null) {
        if (this != null && this < value) {
            validator.addFieldError(fieldName, message ?: "$fieldName must be greater than $value or equals")
        }
    }

    fun <T> Comparable<T>?.shouldBeLessThanOrEquals(value: T, message: String? = null) {
        if (this != null && this > value) {
            validator.addFieldError(fieldName, message ?: "$fieldName must be less than $value or equals")
        }
    }

    fun String?.shouldNotBeBlank(message: String? = null) {
        if (this != null && this.isEmpty()) {
            validator.addFieldError(fieldName, message ?: "$fieldName must not be blank")
        }
    }

    fun String?.shouldHaveMinLength(minLength: Int, message: String? = null) {
        if (this != null && this.length < minLength) {
            validator.addFieldError(fieldName, message ?: "$fieldName length must be greater than or equal to $minLength")
        }
    }

    fun String?.shouldHaveMaxLength(maxLength: Int, message: String? = null) {
        if (this != null && this.length > maxLength) {
            validator.addFieldError(fieldName, message ?: "$fieldName length must be less than or equal to $maxLength")
        }
    }

    fun String?.shouldMatch(regex: String, message: String? = null) {
        if (this != null && !regex.toRegex().matches(this)) {
            validator.addFieldError(fieldName, message ?: "$fieldName must be match to regex $regex")
        }
    }

    fun String?.shouldStartsWith(prefix: String, message: String? = null) {
        if (this != null && !startsWith(prefix)) {
            validator.addFieldError(fieldName, message ?: "$fieldName should starts with $prefix")
        }
    }

    fun Map<String, Any>?.shouldHaveAtMostSize(size: Int, message: String? = null) {
        if (this != null && this.size > size) {
            validator.addFieldError(fieldName, message ?: "$fieldName size must be less or equal than $size")
        }
    }

    fun Long?.shouldBePositive(message: String? = null) {
        if (this != null && this <= 0) {
            validator.addFieldError(fieldName, message ?: "$fieldName must be positive")
        }
    }

    fun <T> T?.shouldNotBe(value: T, message: String? = null) {
        if (this != null && this == value) {
            validator.addFieldError(fieldName, message ?: "$fieldName must not be $value")
        }
    }

    fun <T> Collection<T>?.shouldNotBeEmpty(message: String? = null) {
        if (this != null && isEmpty()) {
            validator.addFieldError(fieldName, message ?: "$fieldName must not be empty")
        }
    }

    fun <T> Collection<T>?.shouldHaveAtMostSize(maxSize: Int, message: String? = null) {
        if (this != null && size > maxSize) {
            validator.addFieldError(fieldName, message ?: "$fieldName size must be less than or equal to $maxSize")
        }
    }

}