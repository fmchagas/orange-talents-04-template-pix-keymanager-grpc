package br.com.fmchagas.key_manager_grpc.chave_pix.remover

import br.com.fmchagas.key_manager_grpc.chave_pix.*
import br.com.fmchagas.key_manager_grpc.grpc.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoverChavePixEndPointTest(
    val repository: ChavePixRepository,
    val clientGrpc: RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub
) {


    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave pix quando existente`() {
        // cenário
        val existente = repository.save(criaChavePixvalida())

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
                    .setClienteId(criaChavePixvalida().clienteId.toString())
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
        val cliente = repository.save(criaChavePixvalida())

        val outroCliente = repository.save(
            ChavePix(
                clienteId = UUID.fromString("c2621916-90b8-4c44-8a73-16e0c5d5cbc7"),
                tipoChave = TipoDeChave.EMAIL,
                chavePix = "cliente@email.com.br",
                tipoConta = TipoDeConta.CORRENTE,
                Conta(agencia = "3030", numero = "555530", titularNome = "Cliente", titularCpf = "73007268011")
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


    @Factory
    class Clientes {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) canal: ManagedChannel): RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub {
            return RemoveChavePixServiceGrpc.newBlockingStub(canal)
        }
    }

    fun criaChavePixvalida() = ChavePix(
        clienteId = UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
        tipoChave = TipoDeChave.CPF,
        chavePix = "73007268010",
        tipoConta = TipoDeConta.CORRENTE,
        Conta(agencia = "1010", numero = "101011", titularNome = "Teste", titularCpf = "73007268010")
    )

}