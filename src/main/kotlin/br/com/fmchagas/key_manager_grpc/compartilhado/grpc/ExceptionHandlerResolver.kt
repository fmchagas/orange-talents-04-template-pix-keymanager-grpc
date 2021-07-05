package br.com.fmchagas.key_manager_grpc.compartilhado.grpc

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerResolver(
    @Inject private val handlers: List<ExceptionHandler<*>>
) {
    private var defaultHandler: ExceptionHandler<Exception> = DefaultExceptionHandler()

    /**
     * Podemos substituir o handler(manipulador) de exceção padrão por meio deste construtor
     * https://docs.micronaut.io/latest/guide/index.html#replaces
     */
    constructor(
        handlers: List<ExceptionHandler<Exception>>,
        defaultHandler: ExceptionHandler<Exception>
    ) : this(handlers) {
        this.defaultHandler = defaultHandler
    }

    fun resolve(e: Exception): ExceptionHandler<*> {
        val foundHandlers = handlers.filter { h -> h.supports(e) }

        if (foundHandlers.size > 1) {
            throw IllegalStateException("Muitos handlers(manipuladores) suportando a mesma exceção '${e.javaClass.name}': $foundHandlers")
        }

        return foundHandlers.firstOrNull() ?: defaultHandler
    }
}
