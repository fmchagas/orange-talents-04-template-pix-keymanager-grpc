package br.com.fmchagas.key_manager_grpc.chave_pix

import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@ValidChavePix
@Introspected
data class NovaChavePix(

    @field:NotBlank @field:ValidUUID
    val clienteId: String?,

    @field:NotNull
    val tipoChave: TipoDeChave?,

    @field:Size(max = 77)
    val chavePix: String?,

    @field:NotNull
    val tipoConta: TipoDeConta?
) {
    fun toModel(conta: Conta) = ChavePix(
        clienteId = UUID.fromString(this.clienteId),
        tipoChave = this.tipoChave!!,
        chavePix = if(this.tipoChave == TipoDeChave.CHAVE_ALEATORIA) UUID.randomUUID().toString() else this.chavePix!!,
        tipoConta = this.tipoConta!!,
        conta = conta
    )
}
