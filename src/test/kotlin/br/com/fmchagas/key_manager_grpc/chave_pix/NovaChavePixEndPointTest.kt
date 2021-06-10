package br.com.fmchagas.key_manager_grpc.chave_pix

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
import java.util.*
import javax.inject.Singleton

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test

@MicronautTest(transactional = false)
internal class NovaChavePixEndPointTest(
    val repository: ChavePixRepository,
    val clientGrpc: NovaChavePixServiceGrpc.NovaChavePixServiceBlockingStub
) {

    @Test
    fun `deve registrar chave pix`() {
        repository.deleteAll()

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
    fun `deve registrar chave pix aleatoria`() {
        repository.deleteAll()

        val response = clientGrpc.registrar(
            NovaChavePixRequest.newBuilder()
                .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                .setTipoChave(TipoChave.CHAVE_ALEATORIA)
                .setChaveDoPix("")
                .setTipoConta(TipoConta.POUPANCA)
                .build()
        )

        with(response) {
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando existente`() {
        val existente = repository.save(
            ChavePix(
                UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
                TipoChave.CPF, "73007268010", TipoConta.POUPANCA,
            )
        )

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registrar(
                NovaChavePixRequest.newBuilder()
                    .setClienteId(existente.chavePix)
                    .setTipoChave(existente.tipoChave)
                    .setChaveDoPix(existente.chavePix)
                    .setTipoConta(existente.tipoConta)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("chave pix já cadastrada no sistema", status.description)
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