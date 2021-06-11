package br.com.fmchagas.key_manager_grpc.chave_pix

import br.com.fmchagas.key_manager_grpc.grpc.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.util.*
import java.util.regex.Pattern
import javax.inject.Singleton

@Singleton
class NovaChavePixEndPoint(
    private val repository: ChavePixRepository
) : NovaChavePixServiceGrpc.NovaChavePixServiceImplBase() {

    override fun registrar(
        request: NovaChavePixRequest?,
        responseObserver: StreamObserver<NovaChavePixResponse>?
    ) {

        if (request?.tipoChave == TipoChave.CPF && !isCpfValid(request.chaveDoPix)) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("CPF deve ser me um formato valido")
                    .asRuntimeException()
            )

            responseObserver?.onCompleted()
            return
        }

        if (request?.tipoChave == TipoChave.TEL_CELULAR && !isTelefoneValid(request.chaveDoPix)) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("telefone deve ser me um formato valido")
                    .asRuntimeException()
            )

            responseObserver?.onCompleted()
            return
        }

        if (request?.tipoChave == TipoChave.EMAIL && !isEmailValid(request.chaveDoPix)) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("email deve ser me um formato valido")
                    .asRuntimeException()
            )

            responseObserver?.onCompleted()
            return
        }


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

        val pixId: String = repository.save(chavePix)?.let { it.pixId.toString() } ?: ""

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
    chavePix = if (this.tipoChave == TipoChave.CHAVE_ALEATORIA) UUID.randomUUID().toString() else this.chaveDoPix,
    tipoConta = TipoConta.valueOf(this.tipoConta.name)
)

fun isEmailValid(email: String): Boolean {
    return Pattern.compile(
        "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
    ).matcher(email).matches()
}

fun isTelefoneValid(telefone: String) = (
        Pattern.compile("^\\+[1-9][0-9]\\d{1,14}$").matcher(telefone).matches()
        )

fun isCpfValid(cpf: String) = (
        Pattern.compile("^[0-9]{11}\$").matcher(cpf).matches()
        )