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
import org.slf4j.LoggerFactory
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

/*
* KeyType recebe como entrada todos os valores do enum TipoDeChave,
* por isso é possivel fazer o mapeamento entre os valores
* */
enum class KeyType(val paraTipoDeChaveDoDominio: TipoDeChave?) {
    CPF(TipoDeChave.CPF),
    RANDOM(TipoDeChave.CHAVE_ALEATORIA),
    EMAIL(TipoDeChave.EMAIL),
    CNPJ(null),
    PHONE(TipoDeChave.TEL_CELULAR);

    init {
        val logger = LoggerFactory.getLogger(this::class.java)
        logger.info("Inicializando: $paraTipoDeChaveDoDominio")
    }

    /* companion(palavra-chave) object(companheiro da classe KeyType) é um Singleton(unica instância em memória)
     * inicializa quando a classe é carregada(resolida)
     *
     * -> carregamento do KeyType e Companion quando? criando método init descobri quando e como é inicializado
     *
     * -> porque a chamada keyType.paraTipoDeChaveDoDominio!! funciona, pq tem mapeamento explicito do enum
     * -> em nenhum momento foi chamado a função by
     * -> o objeto companio não precisa usar para fazer mapeamento de KeyType para TipoDeChave,
     * provavelmente é usado para mapeamento reverso, dado um TipoDeChave saber o KeyType
     * */
    /*companion object{
                                                //(KeyType, paraTipoDeChaveDoDominio)-> Map<TipoDeChave, KeyType>
        private val mapping = KeyType.values().associateBy(KeyType::paraTipoDeChaveDoDominio)
        private val logger = LoggerFactory.getLogger(this::class.java)

        fun by(tipoDeChave: TipoDeChave) : KeyType{
            logger.info("mapping: $mapping")
            logger.info("tipoDeChave: ${tipoDeChave.name}")

            //return KeyType.values().associateBy(KeyType::paraTipoDeChaveDoDominio)[tipoDeChave]!!

            return mapping[tipoDeChave]
                ?: throw IllegalArgumentException("KeyType inválido ou não encontrado para $tipoDeChave")
        }

        init {
            logger.info("iniciando companion...")
            //logger.info("mapping: $mapping")
        }
    }*/
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
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun toChavePixInfo(): ChavePixInfo {
        //logger.info("de $keyType para ${keyType.paraTipoDeChaveDoDominio!!}")

        return ChavePixInfo(
            tipo = keyType.paraTipoDeChaveDoDominio!! /*when (keyType) {
                KeyType.CPF -> TipoDeChave.CPF
                KeyType.RANDOM -> TipoDeChave.CHAVE_ALEATORIA
                KeyType.PHONE -> TipoDeChave.TEL_CELULAR
                KeyType.EMAIL -> TipoDeChave.EMAIL
                KeyType.CNPJ -> TipoDeChave.valueOf("CNPJ")
            }*/,
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