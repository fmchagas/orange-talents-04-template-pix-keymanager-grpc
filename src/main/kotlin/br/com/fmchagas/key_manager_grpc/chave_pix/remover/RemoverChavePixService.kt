package br.com.fmchagas.key_manager_grpc.chave_pix.remover

import br.com.fmchagas.key_manager_grpc.chave_pix.ChavePixRepository
import br.com.fmchagas.key_manager_grpc.compartilhado.exception.NotFoundException
import io.reactivex.annotations.NonNull
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Singleton
open class RemoverChavePixService(
    @Inject val repository: ChavePixRepository
) {

    @Transactional
    open fun remover(@Valid request: RemoveChavePixRequest) {

        val chavePix = buscaPeloPixIdEClienteId(request.pixId, request.clienteId)

        repository.delete(chavePix)
    }

    private fun buscaPeloPixIdEClienteId(@NotBlank pixId: String, @NotBlank clienteId: String) = repository.findByPixIdAndClienteId(
            pixId = UUID.fromString(pixId),
            clienteId = UUID.fromString(clienteId)
        ) ?: throw NotFoundException("Chave Pix não encontrada ou não pertence ao cliente")
}
