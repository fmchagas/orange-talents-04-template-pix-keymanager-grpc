package br.com.fmchagas.key_manager_grpc.compartilhado.grpc.handlers

import br.com.fmchagas.key_manager_grpc.compartilhado.exception.ChavePixExistenteException
import br.com.fmchagas.key_manager_grpc.compartilhado.grpc.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixExistenteExceptionHandler : ExceptionHandler<ChavePixExistenteException> {

    override fun handle(e: ChavePixExistenteException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.ALREADY_EXISTS
                .withDescription(e.message)
                //.withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixExistenteException
    }
}