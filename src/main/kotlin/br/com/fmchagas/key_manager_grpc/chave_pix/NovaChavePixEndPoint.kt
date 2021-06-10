package br.com.fmchagas.key_manager_grpc.chave_pix

import br.com.fmchagas.key_manager_grpc.grpc.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Singleton

@Singleton
class NovaChavePixEndPoint(
    private val repository: ChavePixRepository
) : NovaChavePixServiceGrpc.NovaChavePixServiceImplBase() {

    override fun registrar(
        request: NovaChavePixRequest?,
        responseObserver: StreamObserver<NovaChavePixResponse>?
    ) {

        if (request?.chaveDoPix?.let { repository.existsByChavePix(it) } == true) {
            responseObserver?.onError(
                Status.ALREADY_EXISTS
                    .withDescription("chave pix já cadastrada no sistema")
                    .asRuntimeException()
            )

            responseObserver?.onCompleted()
            return
        }

        val chavePix = request?.toModel()

        val pixId : String = repository.save(chavePix)?.let { it.pixId.toString() } ?: ""

        val response = NovaChavePixResponse.newBuilder()
            .setPixId(pixId)
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}

fun NovaChavePixRequest.toModel() = ChavePix(
    clienteId = UUID.fromString(this.clienteId),
    tipoChave = TipoChave.valueOf(this.tipoChave.name),
    chavePix = if(this.tipoChave == TipoChave.CHAVE_ALEATORIA)  UUID.randomUUID().toString() else this.chaveDoPix,
    tipoConta = TipoConta.valueOf(this.tipoConta.name)
)