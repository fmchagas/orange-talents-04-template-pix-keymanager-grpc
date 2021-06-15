package br.com.fmchagas.key_manager_grpc.compartilhado.grpc

import io.grpc.BindableService
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

/**
 * Class resposavel por interceptar gRPC EndPoints e handling(tratamento) de qualquer exceção lançada por outros métodos
 */
@Singleton
class ExceptionHandlerInterceptor(
    @Inject private val resolver: ExceptionHandlerResolver
) : MethodInterceptor<BindableService, Any>{
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {

        try{
            // proceed() - Prossegue com a invocação, Retorna: O valor de retorno do método
            return context.proceed()
        }catch (e: Exception){
            if(logger.isWarnEnabled)
                logger.warn("Tratando a exceção '${e.javaClass.name}' quando processava a chamada: ${context.targetMethod}", e)

            val handler = resolver.resolve(e)
            val status = handler.handle(e)

            /*(context.parameterValues.get(1) as StreamObserver<*>)
                .onError(status.asRuntimeException())*/

            GrpcEndointArguments(context).response()
                .onError(status.asRuntimeException())

            return null
        }
    }

    /**
     * Representa os argumentos do método do endpoint
     */
    private class GrpcEndointArguments(val context: MethodInvocationContext<BindableService, Any?>) {
        fun response(): StreamObserver<*> {
            return context.parameterValues.get(1) as StreamObserver<*>
        }
    }
}

