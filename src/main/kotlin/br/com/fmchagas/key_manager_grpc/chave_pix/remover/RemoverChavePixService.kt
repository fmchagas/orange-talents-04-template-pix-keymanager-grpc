package br.com.fmchagas.key_manager_grpc.chave_pix.remover

import br.com.fmchagas.key_manager_grpc.chave_pix.ChavePixRepository
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.BcbClient
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.DeletePixKeyRequest
import br.com.fmchagas.key_manager_grpc.compartilhado.exception.NotFoundException
import io.grpc.Status
import io.micronaut.http.HttpStatus
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Singleton
open class RemoverChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val clientBcb: BcbClient
) {

    @Transactional
    open fun remover(@Valid request: RemoveChavePixRequest) {

        val chavePix = buscaPeloPixIdEClienteId(request.pixId, request.clienteId)

        val response = clientBcb.removerViaHttp(chavePix.chavePix,
            DeletePixKeyRequest(
                key = chavePix.chavePix,
                participant = chavePix.conta.instituicaoIsb
            )
        )
        //TODO perguntar o devolver quado 404, 403
        if (response.status != HttpStatus.OK){
            throw IllegalStateException("Erro quando tentamos remover chave pix no banco central")
        }

        repository.delete(chavePix)
    }

    private fun buscaPeloPixIdEClienteId(@NotBlank pixId: String, @NotBlank clienteId: String) = repository.findByPixIdAndClienteId(
            pixId = UUID.fromString(pixId),
            clienteId = UUID.fromString(clienteId)
        ) ?: throw NotFoundException("Chave Pix não encontrada ou não pertence ao cliente")
}
