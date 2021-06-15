package br.com.fmchagas.key_manager_grpc.compartilhado.grpc.handlers

import br.com.fmchagas.key_manager_grpc.compartilhado.exception.NotFoundException
import br.com.fmchagas.key_manager_grpc.compartilhado.grpc.ExceptionHandler
import io.grpc.Status
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class NotFoundExceptionHandler : ExceptionHandler<NotFoundException> {

    override fun handle(e: NotFoundException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is NotFoundException
    }
}