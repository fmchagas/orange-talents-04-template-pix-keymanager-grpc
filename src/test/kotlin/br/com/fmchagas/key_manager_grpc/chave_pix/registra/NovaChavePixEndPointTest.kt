package br.com.fmchagas.key_manager_grpc.chave_pix.registra

import br.com.fmchagas.key_manager_grpc.chave_pix.*
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.*
import br.com.fmchagas.key_manager_grpc.grpc.NovaChavePixRequest
import br.com.fmchagas.key_manager_grpc.grpc.NovaChavePixServiceGrpc
import br.com.fmchagas.key_manager_grpc.grpc.TipoChave
import br.com.fmchagas.key_manager_grpc.grpc.TipoConta
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Inject
import org.mockito.kotlin.*
import java.time.LocalDateTime

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

        whenever(
            clientErpItau.buscaViaHttp(
                "5260263c-a3c1-4727-ae32-3bdb2538841b",
                "CONTA_POUPANCA"
            )
        ).thenReturn(HttpResponse.ok(informacaoDaContaResponseFake()))

        whenever(clientBcb.registrarViaHttp(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponseFake()))

        val response = clientGrpc.registrar(
            NovaChavePixRequest.newBuilder()
                .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                .setTipoChave(TipoChave.CPF)
                .setChaveDoPix("73007268010")
                .setTipoConta(TipoConta.POUPANCA)
                .build()
        )

        with(response) {
            assertNotNull(pixId)
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
        val conta = Conta("1010", "101011", "Teste", "73007268010", "Itau S.A", "1010")

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
            clientErpItau.buscaViaHttp(
                "ea691b01-4567-498b-83b7-1552df6cb1f4",
                "CONTA_POUPANCA"
            )
        ).thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registrar(
                NovaChavePixRequest.newBuilder()
                    .setClienteId("ea691b01-4567-498b-83b7-1552df6cb1f4")
                    .setTipoChave(TipoChave.CPF)
                    .setChaveDoPix("73007268010")
                    .setTipoConta(TipoConta.POUPANCA)
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
            clientErpItau.buscaViaHttp(
                "5260263c-a3c1-4727-ae32-3bdb2538841b",
                "CONTA_POUPANCA"
            )
        ).thenReturn(HttpResponse.ok(informacaoDaContaResponseFake()))

        whenever(clientBcb.registrarViaHttp(createPixKeyRequest()))
            .thenReturn(HttpResponse.unprocessableEntity())

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registrar(
                NovaChavePixRequest.newBuilder()
                    .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .setTipoChave(TipoChave.CPF)
                    .setChaveDoPix("73007268010")
                    .setTipoConta(TipoConta.POUPANCA)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("não foi possivel criar chave pix no banco central", status.description)

        }
    }

    fun informacaoDaContaResponseFake() = InformacaoDaContaResponse(
        tipo = "CONTA_CORRENTE",
        instituicao = InstituicaoResponse(nome = "Banco X", ispb = "1023"),
        agencia = "1010",
        numero = "20202",
        titular = TitularResponse(id = "5260263c-a3c1-4727-ae32-3bdb2538841b", nome = "Dunha", cpf = "73007268010")
    )

    @Factory
    class Clientes {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) canal: ManagedChannel): NovaChavePixServiceGrpc.NovaChavePixServiceBlockingStub {
            return NovaChavePixServiceGrpc.newBlockingStub(canal)
        }
    }
}

fun createPixKeyResponseFake() = CreatePixKeyResponse(
    keyType = KeyType.CPF,
    key = "73007268010",
    bankAccount = BankAccount(
        participant = "1023",
        branch = "0001",
        accountNumber = "20202",
        accountType = BankAccount.AccountType.SVGS
    ),
    owner = Owner(
        type = Owner.OwnerType.NATURAL_PERSON,
        name = "Dunha",
        taxIdNumber = "73007268010"
    ),
    createdAt = LocalDateTime.now()
)

fun createPixKeyRequest() = CreatePixKeyRequest(
    key = "73007268010",
    keyType = KeyType.CPF,
    bankAccount = BankAccount(
        participant = "1023",
        branch = "0001",
        accountNumber = "20202",
        accountType = BankAccount.AccountType.SVGS
    ),
    owner = Owner(
        type = Owner.OwnerType.NATURAL_PERSON,
        name = "Dunha",
        taxIdNumber = "73007268010"
    )
)