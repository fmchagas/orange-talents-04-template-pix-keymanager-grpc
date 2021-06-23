package br.com.fmchagas.key_manager_grpc.chave_pix.clients

import br.com.fmchagas.key_manager_grpc.chave_pix.*
import br.com.fmchagas.key_manager_grpc.chave_pix.consulta.ChavePixInfo
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime
import java.time.ZoneId

@Client("\${bcb.url}")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
interface BcbClient {

    @Post("/api/v1/pix/keys")
    fun registrarViaHttp(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse?>

    @Delete("/api/v1/pix/keys/{key}")
    fun removerViaHttp(
        @PathVariable key: String,
        @Body request: DeletePixKeyRequest
    ): HttpResponse<DeletePixKeyResponse>

    @Get("/api/v1/pix/keys/{key}")
    fun buscarViaHttp(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>
}

data class CreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

/* Remove */
data class DeletePixKeyRequest(
    val key: String,
    val participant: String
)

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

/* Buscar */
data class PixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toChavePixInfo(): ChavePixInfo {
        return ChavePixInfo(
            tipo = keyType.paraTipoDeChaveDoDominio!!,
            chavePix = key,
            tipoDeConta = bankAccount.accountType.paraTipoContaDoDominio,
            conta = Conta(
                agencia = bankAccount.branch,
                numero = bankAccount.accountNumber,
                titularNome = owner.name,
                titularCpf = owner.taxIdNumber,
                instituicaoNome = Instituicoes.nome(bankAccount.participant),
                instituicaoIsb = bankAccount.participant
            ),
            criadoEm = createdAt.atZone(ZoneId.of("America/Cuiaba")).toInstant()
        )
    }
}