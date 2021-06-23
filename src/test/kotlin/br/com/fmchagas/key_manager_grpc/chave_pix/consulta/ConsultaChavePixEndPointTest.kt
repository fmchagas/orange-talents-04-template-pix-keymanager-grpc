package br.com.fmchagas.key_manager_grpc.chave_pix.consulta

import br.com.fmchagas.key_manager_grpc.chave_pix.*
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.*
import br.com.fmchagas.key_manager_grpc.grpc.*
import br.com.fmchagas.key_manager_grpc.util.ChavePixUtil
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultaChavePixEndPointTest(
    val repository: ChavePixRepository,
    val clientGrpc: ConsultarChavePixServiceGrpc.ConsultarChavePixServiceBlockingStub
) {
    @Inject
    private lateinit var clientBcb: BcbClient

    @MockBean(BcbClient::class)
    internal fun mockBcbClient() = mock<BcbClient>()

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
    }

    @ParameterizedTest(name="#{index}- Teste com chave={arguments}")
    @ValueSource(strings = ["", " "])
    fun `nao consultar por chave pix quando parameto for invalido`(strings: String) {

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.consulta(
                ConsultarChavePixRequestGrpc.newBuilder()
                    .setChavePix(strings)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @ParameterizedTest(name="#{index}- Teste com chave={arguments}")
    @ValueSource(strings = ["", " ", "x"])
    fun `nao consultar por pixId e clienteId quando parametos forem invalido`(strings: String) {

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.consulta(
                ConsultarChavePixRequestGrpc.newBuilder()
                    .setPixId(
                        ConsultarChavePixRequestGrpc.FiltroPorPixId.newBuilder()
                            .setPixId(strings)
                            .setClienteId(strings)
                            .build()
                    )
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `nao consultar chave pix quando parametros do filtro nao forem enviados`() {
        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.consulta(
                ConsultarChavePixRequestGrpc.newBuilder()
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Informe a chavePix ou pixId", status.description)
        }
    }

    @Test
    fun `deve retornar chave por pixId e clienteId`() {
        // cenário
        val conta = Conta("1010", "101011", "Teste", "73007268010", "Itau S.A", "1010")

        val existente = repository.save(
            ChavePix(
                UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
                TipoDeChave.CPF, "73007268010", TipoDeConta.POUPANCA, conta,
            )
        )

        // ação
        val response = clientGrpc.consulta(
            ConsultarChavePixRequestGrpc.newBuilder()
                .setPixId(
                    ConsultarChavePixRequestGrpc.FiltroPorPixId.newBuilder()
                        .setPixId(existente.pixId.toString())
                        .setClienteId(existente.clienteId.toString())
                        .build()
                ).build()
        )

        // validação
        with(response) {
            assertEquals(existente.pixId.toString(), this.pixId)
            assertEquals(existente.clienteId.toString(), this.clienteId)
            assertEquals(existente.tipoChave.name, this.chave.tipo.name)
            assertEquals(existente.chavePix, this.chave.chavePix)
        }
    }

    @Test
    fun `deve retornar chave por chavePix quando registro existir localmente`() {
        // cenário
        val conta = Conta("1010", "101011", "Teste", "73007268010", "Itau S.A", "1010")

        val existente = repository.save(
            ChavePix(
                UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
                TipoDeChave.CPF, "73007268010", TipoDeConta.POUPANCA, conta,
            )
        )

        // ação
        val response = clientGrpc.consulta(
            ConsultarChavePixRequestGrpc.newBuilder()
                .setChavePix(existente.chavePix)
                .build()
        )

        // validação
        with(response) {
            assertEquals(existente.pixId.toString(), this.pixId)
            assertEquals(existente.clienteId.toString(), this.clienteId)
            assertEquals(existente.tipoChave.name, this.chave.tipo.name)
            assertEquals(existente.chavePix, this.chave.chavePix)
        }
    }

    @Test
    fun `deve retornar chave por chavePix quando registro nao existir localmente mas existe no banco central`() {
        // cenário
        val bcbResponse = ChavePixUtil.pixKeyDetailsResponse()

        whenever(clientBcb.buscarViaHttp(key = "fernando@gmail.com"))
            .thenReturn(HttpResponse.ok(bcbResponse))


        // ação
        val response = clientGrpc.consulta(
            ConsultarChavePixRequestGrpc.newBuilder()
                .setChavePix("fernando@gmail.com")
                .build()
        )

        // validação
        with(response) {
            assertEquals("", this.pixId)
            assertEquals("", this.clienteId)
            assertEquals(bcbResponse.keyType.name, this.chave.tipo.name)
            assertEquals(bcbResponse.key, this.chave.chavePix)
            assertEquals(bcbResponse.bankAccount.accountNumber, this.chave.conta.conta)
            assertEquals(bcbResponse.bankAccount.branch, this.chave.conta.agencia)
        }
    }

    @Test
    fun `nao deve retornar chave por chavePix quando registro nao existir no local e no banco central`() {
        // cenário
        whenever(clientBcb.buscarViaHttp(key = "nao.existe@gmail.com"))
            .thenReturn(HttpResponse.notFound())


        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.consulta(
                ConsultarChavePixRequestGrpc.newBuilder()
                    .setChavePix("nao.existe@gmail.com")
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontada", status.description)
        }
    }

    @Test
    fun `nao deve retornar chave por pixId e clienteId quando registro nao existir`() {
        // cenário

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.consulta(
                ConsultarChavePixRequestGrpc.newBuilder()
                    .setPixId(
                        ConsultarChavePixRequestGrpc.FiltroPorPixId.newBuilder()
                            .setPixId("f105b416-94e7-4ee8-b5e2-10a467fcb1b2")
                            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                            .build()
                    ).build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontada ou não pertence ao cliente", status.description)
        }
    }


    @Factory
    class Clientes {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) canal: ManagedChannel): ConsultarChavePixServiceGrpc.ConsultarChavePixServiceBlockingStub {
            return ConsultarChavePixServiceGrpc.newBlockingStub(canal)
        }
    }
}