package br.com.fmchagas.key_manager_grpc.chave_pix.registra

import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeChave
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeConta
import br.com.fmchagas.key_manager_grpc.compartilhado.grpc.ErrorHandler
import br.com.fmchagas.key_manager_grpc.grpc.*
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class NovaChavePixEndPoint(
    @Inject private val novaChavePixService: NovaChavePixService
) : NovaChavePixServiceGrpc.NovaChavePixServiceImplBase() {

    override fun registrar(
        request: NovaChavePixRequest?,
        responseObserver: StreamObserver<NovaChavePixResponse>?
    ) {

        val novaChavePix = request?.toModelRequest()
        val chavePixSalva = novaChavePixService.registrar(novaChavePix!!)

        val response = NovaChavePixResponse.newBuilder()
            .setPixId(chavePixSalva.pixId.toString())
            .build()

        responseObserver?.onNext(response)
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