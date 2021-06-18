package br.com.fmchagas.key_manager_grpc.chave_pix.clients

import br.com.fmchagas.key_manager_grpc.chave_pix.Conta
import br.com.fmchagas.key_manager_grpc.chave_pix.Instituicoes
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeChave
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeConta
import br.com.fmchagas.key_manager_grpc.chave_pix.consulta.ChavePixInfo
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import org.hibernate.annotations.GeneratorType
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

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

data class CreatePixKeyRequest(
    val keyType: KeyType,
    var key: String?,
    val bankAccount: BankAccount,
    val owner: Owner
) {


}

data class CreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

enum class KeyType {
    CPF,
    RANDOM,
    EMAIL,
    CNPJ,
    PHONE
}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {

    enum class AccountType {
        CACC,
        SVGS
    }
}

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {

    enum class OwnerType {
        NATURAL_PERSON
    }
}

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
            tipo = when (keyType) {
                KeyType.CPF -> TipoDeChave.CPF
                KeyType.RANDOM -> TipoDeChave.CHAVE_ALEATORIA
                KeyType.PHONE -> TipoDeChave.TEL_CELULAR
                KeyType.EMAIL -> TipoDeChave.EMAIL
                KeyType.CNPJ -> TipoDeChave.valueOf("CNPJ")
            },
            chavePix = key,
            tipoDeConta = when (bankAccount.accountType) {
                BankAccount.AccountType.CACC -> TipoDeConta.CORRENTE
                BankAccount.AccountType.SVGS -> TipoDeConta.POUPANCA
            },
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