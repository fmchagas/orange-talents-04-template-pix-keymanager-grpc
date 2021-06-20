package br.com.fmchagas.key_manager_grpc.chave_pix.lista

import br.com.fmchagas.key_manager_grpc.chave_pix.*
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.BcbClient
import br.com.fmchagas.key_manager_grpc.grpc.ConsultarChavePixRequestGrpc
import br.com.fmchagas.key_manager_grpc.grpc.ConsultarChavePixServiceGrpc
import br.com.fmchagas.key_manager_grpc.grpc.ListaChavePixRequestGrpc
import br.com.fmchagas.key_manager_grpc.grpc.ListarChavePixServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert
import org.hibernate.validator.internal.util.Contracts
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListaChavesPixEndPointTest(
    val repository: ChavePixRepository,
    val clientGrpc: ListarChavePixServiceGrpc.ListarChavePixServiceBlockingStub
){

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
    }

    @ParameterizedTest(name="#{index}- Teste com clientId={arguments}")
    @ValueSource(strings = ["", " "])
    fun `nao listar chaves pix quando parametro for invalido`(strings: String){
        // cenário é o argumento 'strings' do parameto

        // ação
        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.lista(
                ListaChavePixRequestGrpc.newBuilder()
                    .setClientId(strings)
                    .build()
            )
        }

        // validação
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O parâmetro de entrada clienteId não pode ser vazio", status.description)
        }
    }

    @Test
    fun `listar chaves pix quando encontrar registro`(){
        // cenário
        val conta = Conta("1010", "101011", "Teste", "73007268010", "Itau S.A", "1010")

        val existente = repository.save(
            ChavePix(
                UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
                TipoDeChave.CPF, "73007268010", TipoDeConta.POUPANCA, conta,
            )
        )

        // ação
        val response = clientGrpc.lista(
            ListaChavePixRequestGrpc.newBuilder()
                .setClientId(existente.clienteId.toString())
                .build()
        )

        // validação
        with(response.chavesList) {
            assertEquals(1, this.size)
            assertEquals(existente.pixId.toString(), this[0].pixId)
        }
    }

    @Test
    fun `nao deve listar chaves pix quando nao encontrar registro`(){
        // cenário
        val clienteId = UUID.randomUUID().toString()

        //ação
        val response = clientGrpc.lista(
            ListaChavePixRequestGrpc.newBuilder()
                .setClientId(clienteId)
                .build()
        )

        // validação
        assertEquals(0, response.chavesList.size)
    }



    @Factory
    class Clientes {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) canal: ManagedChannel): ListarChavePixServiceGrpc.ListarChavePixServiceBlockingStub {
            return ListarChavePixServiceGrpc.newBlockingStub(canal)
        }
    }
}