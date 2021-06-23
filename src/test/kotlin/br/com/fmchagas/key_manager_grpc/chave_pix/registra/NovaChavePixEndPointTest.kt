package br.com.fmchagas.key_manager_grpc.chave_pix.registra

import br.com.fmchagas.key_manager_grpc.chave_pix.*
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.*
import br.com.fmchagas.key_manager_grpc.grpc.NovaChavePixRequest
import br.com.fmchagas.key_manager_grpc.grpc.NovaChavePixServiceGrpc
import br.com.fmchagas.key_manager_grpc.grpc.TipoChave
import br.com.fmchagas.key_manager_grpc.grpc.TipoConta
import br.com.fmchagas.key_manager_grpc.util.ChavePixUtil
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import javax.inject.Singleton
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import org.mockito.kotlin.*

@MicronautTest(transactional = false)
internal class NovaChavePixEndPointTest(
    val repository: ChavePixRepository,
    val clientGrpc: NovaChavePixServiceGrpc.NovaChavePixServiceBlockingStub
) {
    @Inject
    private lateinit var clientErpItau: InformacaoDasContasDoItauERPClient

    @Inject
    private lateinit var clientBcb: BcbClient

    @MockBean(InformacaoDasContasDoItauERPClient::class)
    internal fun mockItauClient() = mock<InformacaoDasContasDoItauERPClient>()

    @MockBean(BcbClient::class)
    internal fun mockBcbClient() = mock<BcbClient>()

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar chave pix`() {
        // cenário
        Mockito.`when`(clientErpItau.buscaViaHttp(clienteId = ChavePixUtil.CLIENTE_ID, tipo = ChavePixUtil.TIPO_CONTA))
            .thenReturn(HttpResponse.ok(ChavePixUtil.informacaoDaContaResponse()))

        Mockito.`when`(clientBcb.registrarViaHttp(ChavePixUtil.request()))
            .thenReturn(HttpResponse.created(ChavePixUtil.createPixKeyResponse()))

        // ação
        val response = clientGrpc.registrar(
            NovaChavePixRequest.newBuilder()
                .setClienteId(ChavePixUtil.CLIENTE_ID)
                .setTipoChave(TipoChave.EMAIL)
                .setChaveDoPix(ChavePixUtil.CHAVE)
                .setTipoConta(TipoConta.CORRENTE)
                .build()
        )

        // validação
        with(response) {
            Assertions.assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametos forem invalidos`() {
        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registrar(
                NovaChavePixRequest.newBuilder()
                    .setClienteId("invalido")
                    .setTipoChave(TipoChave.CPF)
                    .setChaveDoPix("")
                    .setTipoConta(TipoConta.POUPANCA)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando existente`() {
        val conta = Conta("1010", "101011", "Fernando", "73007268010", "UNIBANCO ITAU", "1010")

        val existente = repository.save(
            ChavePix(
                UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
                TipoDeChave.CPF, "73007268010", TipoDeConta.POUPANCA, conta,
            )
        )

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registrar(
                NovaChavePixRequest.newBuilder()
                    .setClienteId(existente.clienteId.toString())
                    .setTipoChave(TipoChave.CPF)
                    .setChaveDoPix(existente.chavePix)
                    .setTipoConta(TipoConta.POUPANCA)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("chave pix '${existente.chavePix}' já cadastrada no sistema", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando conta do cliente nao for encontrada`() {
        whenever(
            clientErpItau.buscaViaHttp(clienteId = ChavePixUtil.CLIENTE_ID, tipo = ChavePixUtil.TIPO_CONTA)
        ).thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registrar(
                NovaChavePixRequest.newBuilder()
                    .setClienteId(ChavePixUtil.CLIENTE_ID)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChaveDoPix(ChavePixUtil.CHAVE)
                    .setTipoConta(TipoConta.CORRENTE)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Conta do cliente não encontrada", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando conta do cliente nao for registrada no banco central`() {

        whenever(
            clientErpItau.buscaViaHttp(clienteId = ChavePixUtil.CLIENTE_ID, tipo = ChavePixUtil.TIPO_CONTA)
        ).thenReturn(HttpResponse.ok(ChavePixUtil.informacaoDaContaResponse()))

        whenever(clientBcb.registrarViaHttp(ChavePixUtil.request()))
            .thenReturn(HttpResponse.unprocessableEntity())

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registrar(
                NovaChavePixRequest.newBuilder()
                    .setClienteId(ChavePixUtil.CLIENTE_ID)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChaveDoPix(ChavePixUtil.CHAVE)
                    .setTipoConta(TipoConta.CORRENTE)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("não foi possivel criar chave pix no banco central", status.description)

        }
    }

    @Factory
    class Clientes {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) canal: ManagedChannel): NovaChavePixServiceGrpc.NovaChavePixServiceBlockingStub {
            return NovaChavePixServiceGrpc.newBlockingStub(canal)
        }
    }
}