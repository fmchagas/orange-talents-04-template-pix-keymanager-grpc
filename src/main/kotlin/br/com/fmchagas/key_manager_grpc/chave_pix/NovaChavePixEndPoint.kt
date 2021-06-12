package br.com.fmchagas.key_manager_grpc.chave_pix

import br.com.fmchagas.key_manager_grpc.grpc.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.lang.Exception
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NovaChavePixEndPoint(
    @Inject val novaChavePixService: NovaChavePixService
) : NovaChavePixServiceGrpc.NovaChavePixServiceImplBase() {

    override fun registrar(
        request: NovaChavePixRequest?,
        responseObserver: StreamObserver<NovaChavePixResponse>?
    ) {

        val novaChavePix = request?.toModelRequest()

        try {
            val chavePixSalva = novaChavePixService.registrar(novaChavePix!!)
            val response = NovaChavePixResponse.newBuilder()
                .setPixId(chavePixSalva.pixId.toString())
                .build()

            responseObserver?.onNext(response)
        } catch (e: UnsupportedOperationException) {
            responseObserver?.onError(
                Status.ALREADY_EXISTS
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        } catch (e: RuntimeException) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        }

        responseObserver?.onCompleted()
    }
}

fun NovaChavePixRequest.toModelRequest() = NovaChavePix(
    clienteId = clienteId,
    tipoChave = when (tipoChave) {
        TipoChave.UN_KNOWN_TIPO_CHAVE -> null
        else -> TipoDeChave.valueOf(tipoChave.name)
    },
    chavePix = chaveDoPix,
    tipoConta = when (tipoConta) {
        TipoConta.UN_KNOWN_TIPO_CONTA -> null
        else -> TipoDeConta.valueOf(tipoConta.name)
    }
)