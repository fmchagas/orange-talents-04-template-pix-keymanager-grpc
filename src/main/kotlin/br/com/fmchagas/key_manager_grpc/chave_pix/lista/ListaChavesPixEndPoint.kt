package br.com.fmchagas.key_manager_grpc.chave_pix.list

import br.com.fmchagas.key_manager_grpc.chave_pix.ChavePixRepository
import br.com.fmchagas.key_manager_grpc.compartilhado.grpc.ErrorHandler
import br.com.fmchagas.key_manager_grpc.grpc.*
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ListaChavesPixEndPoint(
    @Inject private val repository: ChavePixRepository
) : ListarChavePixServiceGrpc.ListarChavePixServiceImplBase() {
    override fun lista(
        request: ListaChavePixRequestGrpc,
        responseObserver: StreamObserver<ListaChavePixResponseGrpc>
    ) {
        if (request.clientId.isNullOrEmpty() || request.clientId.isBlank()) {
            throw IllegalArgumentException("O parâmetro de entrada clienteId não pode ser vazio")
        }

        val chavesPix = repository.findAllByClienteId(
            UUID.fromString(request.clientId)
        ).map { chave ->
            ListaChavePixResponseGrpc.ListaChavePixGrpc.newBuilder()
                .setPixId(chave.pixId.toString())
                .setTipoChave(TipoChave.valueOf(chave.tipoChave.name))
                .setChavePix(chave.chavePix)
                .setTipoConta(TipoConta.valueOf(chave.tipoConta.name))
                .setCriadoEm(
                    Timestamp.newBuilder()
                        .setSeconds(chave.criadoEm.epochSecond)
                        .setNanos(chave.criadoEm.nano)
                        .build()
                )
                .build()
        }

        val response = ListaChavePixResponseGrpc.newBuilder()
            .setClientId(request.clientId)
            .addAllChaves(chavesPix)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}