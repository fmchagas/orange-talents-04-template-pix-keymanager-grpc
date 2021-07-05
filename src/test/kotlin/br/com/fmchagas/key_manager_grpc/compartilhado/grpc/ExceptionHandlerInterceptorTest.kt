package br.com.fmchagas.key_manager_grpc.compartilhado.grpc

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInvocationContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.notNull
import org.mockito.kotlin.verify
import java.lang.RuntimeException

@ExtendWith(MockitoExtension::class)
internal class ExceptionHandlerInterceptorTest{

    @Mock
    lateinit var context: MethodInvocationContext<BindableService, Any?>

    val interceptor = ExceptionHandlerInterceptor(resolver = ExceptionHandlerResolver(handlers = emptyList()))

    @Test
    fun `deve capturar a excecao lancada por metodo, e gerar um erro na resposta gRPC`(@Mock streamObserver: StreamObserver<*>) {
        // cenário
        with(context){
            Mockito.`when`(proceed()).thenThrow(RuntimeException("argh!"))
            Mockito.`when`(parameterValues).thenReturn(arrayOf(null, streamObserver))
        }

        // ação
        interceptor.intercept(context)

        // verificação
        verify(streamObserver).onError(notNull())
    }

    @Test
    fun `deve retornar a mesma resposta qunado o metodo nao gerar nenhuma excecao`(){
        val metodoChamado = "cadastrar"

        Mockito.`when`(context.proceed()).thenReturn(metodoChamado)

        assertEquals(metodoChamado, interceptor.intercept(context))
    }
}