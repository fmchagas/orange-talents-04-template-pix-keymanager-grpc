package br.com.fmchagas.key_manager_grpc.chave_pix.remover

import br.com.fmchagas.key_manager_grpc.compartilhado.validator.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class RemoveChavePixRequest(
    @field:NotBlank @field:ValidUUID
    val clienteId: String,

    @field:NotBlank @field:ValidUUID
    val pixId: String
)
