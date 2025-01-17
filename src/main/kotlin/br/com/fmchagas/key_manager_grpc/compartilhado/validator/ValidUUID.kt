package br.com.fmchagas.key_manager_grpc.compartilhado.validator

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import java.util.*
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ReportAsSingleViolation
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*


@ReportAsSingleViolation
@Constraint(validatedBy = [])
@Retention(RUNTIME)
@Target(FIELD, CONSTRUCTOR, PROPERTY, VALUE_PARAMETER)
annotation class ValidUUID(
    val message: String = "não é um formato válido de UUID"
    //val groups: Array<KClass<Any>> = [],
    //val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidUUIDValidator: ConstraintValidator<ValidUUID, String> {
    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<ValidUUID>,
        context: ConstraintValidatorContext
    ): Boolean {
        if(value.isNullOrEmpty()) return true

        try{
            /*
            * levanta IllegalArgumentException se UUID inválido
             */
            UUID.fromString(value)
            return true
        }catch (e: IllegalArgumentException){
            return false
        }
    }
}
