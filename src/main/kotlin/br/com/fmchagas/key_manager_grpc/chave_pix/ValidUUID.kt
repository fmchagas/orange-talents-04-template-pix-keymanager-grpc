package br.com.fmchagas.key_manager_grpc.chave_pix

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import java.lang.IllegalArgumentException
import java.lang.annotation.ElementType
import java.util.*
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*


/*@Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
    flags = [Pattern.Flag.CASE_INSENSITIVE]
)*/
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
