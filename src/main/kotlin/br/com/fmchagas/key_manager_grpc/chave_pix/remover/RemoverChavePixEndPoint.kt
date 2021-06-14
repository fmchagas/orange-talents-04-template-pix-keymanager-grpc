package br.com.fmchagas.key_manager_grpc.chave_pix.remover


import br.com.fmchagas.key_manager_grpc.compartilhado.exception.NotFoundException
import br.com.fmchagas.key_manager_grpc.grpc.RemoveChavePixRequestGrpc
import br.com.fmchagas.key_manager_grpc.grpc.RemoveChavePixResponseGrpc
import br.com.fmchagas.key_manager_grpc.grpc.RemoveChavePixServiceGrpc
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoverChavePixEndPoint(
    @Inject private val service: RemoverChavePixService
) : RemoveChavePixServiceGrpc.RemoveChavePixServiceImplBase() {

    override fun remover(
        request: RemoveChavePixRequestGrpc,
        responseObserver: StreamObserver<RemoveChavePixResponseGrpc>?
    ) {

        try{
            service.remover(request.toRemoveChavePixRequest())

            val response = RemoveChavePixResponseGrpc.newBuilder()
                .setMensagem("Removido com sucesso")
                .build()

            responseObserver?.onNext(response)
        }catch (e: NotFoundException) {
            responseObserver?.onError(
                Status.NOT_FOUND
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        }catch (e: RuntimeException) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        }

        responseObserver?.onCompleted()
    }
}

fun RemoveChavePixRequestGrpc.toRemoveChavePixRequest() = RemoveChavePixRequest(
    clienteId = clienteId,
    pixId = pixId
)
