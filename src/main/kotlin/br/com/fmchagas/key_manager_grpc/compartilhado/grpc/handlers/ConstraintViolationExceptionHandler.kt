package br.com.fmchagas.key_manager_grpc.compartilhado.grpc.handlers

import br.com.fmchagas.key_manager_grpc.compartilhado.grpc.ExceptionHandler
import com.google.protobuf.Any
import com.google.rpc.BadRequest
import com.google.rpc.Code
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

/**
 * Erros da Bean Validation com metadados
 */
@Singleton
class ConstraintViolationExceptionHandler : ExceptionHandler<ConstraintViolationException> {
    override fun handle(e: ConstraintViolationException): ExceptionHandler.StatusWithDetails {
        val details = BadRequest.newBuilder()
            .addAllFieldViolations(
                e.constraintViolations.map {
                    BadRequest.FieldViolation.newBuilder()
                        .setField(it.propertyPath.last().name)
                        .setDescription(it.message)
                        .build()
                }
            )
            .build()

        val statusDetails = com.google.rpc.Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("Dados inválidos")
            .addDetails(Any.pack(details))
            .build()

        return ExceptionHandler.StatusWithDetails(statusDetails)
    }

    override fun supports(e: Exception) = e is ConstraintViolationException
}