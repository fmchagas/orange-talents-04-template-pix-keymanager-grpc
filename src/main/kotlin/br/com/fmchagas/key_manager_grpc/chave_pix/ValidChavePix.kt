package br.com.fmchagas.key_manager_grpc.chave_pix

import br.com.fmchagas.key_manager_grpc.chave_pix.registra.NovaChavePix
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidChavePixValidator::class])
annotation class ValidChavePix(
    val message: String = "chave Pix inválida",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidChavePixValidator: ConstraintValidator<ValidChavePix, NovaChavePix>{
    override fun isValid(
        value: NovaChavePix?,
        annotationMetadata: AnnotationValue<ValidChavePix>,
        context: ConstraintValidatorContext
    ): Boolean {
        if(value?.tipoChave == null) return false

        return value.tipoChave.valida(value.chavePix)
    }
}