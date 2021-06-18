package br.com.fmchagas.key_manager_grpc.chave_pix.consulta

import br.com.fmchagas.key_manager_grpc.chave_pix.ChavePixRepository
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.BcbClient
import br.com.fmchagas.key_manager_grpc.compartilhado.grpc.ErrorHandler
import br.com.fmchagas.key_manager_grpc.grpc.ConsultarChavePixRequestGrpc
import br.com.fmchagas.key_manager_grpc.grpc.ConsultarChavePixResponseGrpc
import br.com.fmchagas.key_manager_grpc.grpc.ConsultarChavePixServiceGrpc
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator
import kotlin.IllegalStateException

@ErrorHandler
@Singleton
class ConsultaChavePixEndPoint(
    @Inject private val service: FiltroService
) : ConsultarChavePixServiceGrpc.ConsultarChavePixServiceImplBase() {

    override fun consulta(
        request: ConsultarChavePixRequestGrpc,
        responseObserver: StreamObserver<ConsultarChavePixResponseGrpc>
    ) {

        val chavePixInfo : ChavePixInfo = when(request.filtroCase){
            ConsultarChavePixRequestGrpc.FiltroCase.CHAVEPIX -> {
                service.buscarPorChavePix(request.chavePix)
            }
            ConsultarChavePixRequestGrpc.FiltroCase.PIXID -> {
                service.buscarPorPixIdEClienteId(request.pixId.clienteId, request.pixId.pixId)
            }
            ConsultarChavePixRequestGrpc.FiltroCase.FILTRO_NOT_SET -> throw IllegalStateException("Informe a chavePix ou pixId")
        }

        val response = chavePixInfo.let {
             ConsultaChavePixResponseConverter().converte(it)
        }

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}