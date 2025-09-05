package ru.kcheranev.trading.core.util

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.raise.Raise
import arrow.core.raise.either
import org.slf4j.LoggerFactory
import ru.kcheranev.trading.core.error.ValidationError
import java.math.BigDecimal

class Validator(private val fieldPrefix: String? = null) {

    val errors: MutableList<String> = mutableListOf()

    fun addError(error: String) {
        errors.add(error)
    }

    fun addErrors(errors: List<String>) {
        this.errors.addAll(errors)
    }

    fun field(fieldName: String, doValidate: FieldValidator.() -> Unit) {
        val fieldValidator = FieldValidator(this, fieldPrefix, fieldName)
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
                if (validator.errors.isNotEmpty()) {
                    raise(ValidationError(validator.errors))
                }
            }

    }

}

class FieldValidator(
    private val validator: Validator,
    prefix: String?,
    fieldName: String
) {

    private val fieldPath = if (prefix == null) fieldName else "$prefix.$fieldName"

    fun String?.shouldNotBeBlank() {
        if (this != null && this.isEmpty()) {
            validator.addError("$fieldPath must not be blank")
        }
    }

    fun String?.shouldHaveMinLength(minLength: Int) {
        if (this != null && this.length < minLength) {
            validator.addError("$fieldPath length must be greater than or equal to $minLength")
        }
    }

    fun String?.shouldHaveMaxLength(maxLength: Int) {
        if (this != null && this.length > maxLength) {
            validator.addError("$fieldPath length must be less than or equal to $maxLength")
        }
    }

    fun String?.shouldMatch(regex: String) {
        if (this != null && !regex.toRegex().matches(this)) {
            validator.addError("$fieldPath must be match to regex $regex")
        }
    }

    fun String?.shouldNotBeNullOrBlank() {
        shouldNotBeNull()
        shouldNotBeBlank()
    }

    fun String?.shouldStartsWith(prefix: String) {
        if (this != null && !startsWith(prefix)) {
            validator.addError("$fieldPath should starts with $prefix")
        }
    }

    fun Any?.shouldNotBeNull() {
        if (this == null) {
            validator.addError("$fieldPath must not be null")
        }
    }

    fun Map<String, Any>?.shouldHaveAtMostSize(size: Int) {
        if (this != null && this.size > size) {
            validator.addError("$fieldPath size must be less or equal than $size")
        }
    }

    fun Long?.shouldBePositive() {
        if (this != null && this <= 0) {
            validator.addError("$fieldPath must be positive")
        }
    }

    fun BigDecimal?.shouldBeLessThan(value: BigDecimal) {
        if (this != null && this > value) {
            validator.addError("$fieldPath must be less than $value")
        }
    }

    fun <T> T?.shouldNotBe(value: T) {
        if (this != null && this == value) {
            validator.addError("$fieldPath must not be $value")
        }
    }

    fun <T> Collection<T>?.shouldNotBeNullOrEmpty() {
        shouldNotBeNull()
        shouldNotBeEmpty()
    }

    fun <T> Collection<T>?.shouldNotBeEmpty() {
        if (this != null && isEmpty()) {
            validator.addError("$fieldPath must not be empty")
        }
    }

    fun <T> Collection<T>?.shouldHaveAtMostSize(maxSize: Int) {
        if (this != null && size > maxSize) {
            validator.addError("$fieldPath size must be less than or equal to $maxSize")
        }
    }

}

fun Raise<ValidationError>.validate(doValidate: Validator.() -> Unit) {
    Validator.validate { doValidate }.onLeft { error -> raise(error) }
}