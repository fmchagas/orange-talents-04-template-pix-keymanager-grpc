package br.com.fmchagas.key_manager_grpc.chave_pix.clients

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${bcb.url}")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
interface BcbClient {

    @Post("/api/v1/pix/keys")
    fun registrarViaHttp(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse?>

    @Delete("/api/v1/pix/keys/{key}")
    fun removerViaHttp(@PathVariable key: String, @Body request: DeletePixKeyRequest) : HttpResponse<DeletePixKeyResponse>
}

data class CreatePixKeyRequest(
    val keyType: KeyType,
    var key: String?,
    val bankAccount: BankAccount,
    val owner: Owner
){


}

data class CreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

enum class KeyType{
    CPF, RANDOM, EMAIL, CNPJ, PHONE
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
){

    enum class OwnerType {
        NATURAL_PERSON
    }
}

/*Remove*/
data class DeletePixKeyRequest(
    val key: String,
    val participant: String
)

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)