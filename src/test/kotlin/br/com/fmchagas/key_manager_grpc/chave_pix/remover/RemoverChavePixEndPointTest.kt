package br.com.fmchagas.key_manager_grpc.chave_pix.remover

import br.com.fmchagas.key_manager_grpc.chave_pix.*
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.BcbClient
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.DeletePixKeyRequest
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID
import javax.inject.Inject

import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoverChavePixEndPointTest(
    val repository: ChavePixRepository,
    val clientGrpc: RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub
) {

    @Inject
    private lateinit var clientBcb: BcbClient

    @MockBean(BcbClient::class)
    internal fun mockBcbClient() = mock<BcbClient>()

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave pix quando existente`() {
        // cenário
        val existente = repository.save(ChavePixUtil.criaChavePixvalidaTipoCpf())

        whenever(
            clientBcb.removerViaHttp(
                existente.chavePix,
                DeletePixKeyRequest(
                    key = existente.chavePix,
                    participant = existente.conta.instituicaoIsb
                )
            )
        ).thenReturn(HttpResponse.ok())

        // ação
        val response = clientGrpc.remover(
            RemoveChavePixRequestGrpc.newBuilder()
                .setClienteId(existente.clienteId.toString())
                .setPixId(existente.pixId.toString())
                .build()
        )

        // validação
        with(response) {
            assertEquals("Removido com sucesso", mensagem)
        }
    }

    @Test
    fun `deve retornar not found quando chave pix nao existir`() {

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.remover(
                RemoveChavePixRequestGrpc.newBuilder()
                    .setClienteId(ChavePixUtil.CLIENTE_ID)
                    .setPixId("c2621916-90b8-4c44-8a73-16e0c5d5cbc7")
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix quando existente mas pertence a outro cliente`() {
        // cenário
        val cliente = repository.save(ChavePixUtil.criaChavePixvalidaTipoCpf())

        val outroCliente = repository.save(
            ChavePix(
                clienteId = UUID.fromString("c2621916-90b8-4c44-8a73-16e0c5d5cbc7"),
                tipoChave = TipoDeChave.EMAIL,
                chavePix = "cliente@email.com.br",
                tipoConta = TipoDeConta.CORRENTE,
                Conta(
                    agencia = "1010",
                    numero = "101011",
                    titularNome = "Teste",
                    titularCpf = "73007268010",
                    instituicaoNome = "Banco X",
                    instituicaoIsb = "1023"
                )
            )
        )

        // ação
        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.remover(
                RemoveChavePixRequestGrpc.newBuilder()
                    .setClienteId(outroCliente.clienteId.toString())
                    .setPixId(cliente.pixId.toString())
                    .build()
            )
        }

        // validação
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix existente quando dar erro no servico do banco central`() {
        // cenário
        val existente = repository.save(ChavePixUtil.criaChavePixvalidaTipoCpf())

        whenever(
            clientBcb.removerViaHttp(
                existente.chavePix,
                DeletePixKeyRequest(
                    key = existente.chavePix,
                    participant = existente.conta.instituicaoIsb
                )
            )
        ).thenReturn(HttpResponse.notFound())

        // ação
        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.remover(
            RemoveChavePixRequestGrpc.newBuilder()
                .setClienteId(existente.clienteId.toString())
                .setPixId(existente.pixId.toString())
                .build()
        )}

        // validação
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro quando tentamos remover chave pix no banco central", status.description)
        }
    }


    @Factory
    class Clientes {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) canal: ManagedChannel): RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub {
            return RemoveChavePixServiceGrpc.newBlockingStub(canal)
        }
    }
}