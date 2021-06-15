package br.com.fmchagas.key_manager_grpc.chave_pix.remover


import br.com.fmchagas.key_manager_grpc.compartilhado.exception.NotFoundException
import br.com.fmchagas.key_manager_grpc.compartilhado.grpc.ErrorHandler
import br.com.fmchagas.key_manager_grpc.grpc.RemoveChavePixRequestGrpc
import br.com.fmchagas.key_manager_grpc.grpc.RemoveChavePixResponseGrpc
import br.com.fmchagas.key_manager_grpc.grpc.RemoveChavePixServiceGrpc
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler//1
@Singleton
class RemoverChavePixEndPoint(
    @Inject private val service: RemoverChavePixService
) : RemoveChavePixServiceGrpc.RemoveChavePixServiceImplBase() {

    override fun remover(
        request: RemoveChavePixRequestGrpc,
        responseObserver: StreamObserver<RemoveChavePixResponseGrpc>?
    ) {

        service.remover(request.toRemoveChavePixRequest())

        val response = RemoveChavePixResponseGrpc.newBuilder()
            .setMensagem("Removido com sucesso")
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}

fun RemoveChavePixRequestGrpc.toRemoveChavePixRequest() = RemoveChavePixRequest(
    clienteId = clienteId,
    pixId = pixId
)
