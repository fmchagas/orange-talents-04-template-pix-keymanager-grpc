package br.com.fmchagas.key_manager_grpc.chave_pix.clients

import br.com.fmchagas.key_manager_grpc.chave_pix.ChavePix
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeChave
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeConta

data class CreatePixKeyRequest(
    val keyType: KeyType,
    var key: String?,
    val bankAccount: BankAccount,
    val owner: Owner
) {
    companion object{
        fun paraModeloBcb(chavePix: ChavePix) : CreatePixKeyRequest {
            return CreatePixKeyRequest(
                keyType = KeyType.by(chavePix.tipoChave),
                key = chavePix.chavePix,
                bankAccount = BankAccount(
                    participant = chavePix.conta.instituicaoIsb, //ispb itau 60701190
                    branch = chavePix.conta.agencia,
                    accountNumber = chavePix.conta.numero,
                    accountType = BankAccount.AccountType.by(chavePix.tipoConta)
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = chavePix.conta.titularNome,
                    taxIdNumber = chavePix.conta.titularCpf
                )
            )
        }
    }
}

enum class KeyType(val paraTipoDeChaveDoDominio: TipoDeChave?) {
    CPF(TipoDeChave.CPF),
    RANDOM(TipoDeChave.CHAVE_ALEATORIA),
    EMAIL(TipoDeChave.EMAIL),
    CNPJ(null),
    PHONE(TipoDeChave.TEL_CELULAR);

    companion object{
        //(KeyType, paraTipoDeChaveDoDominio)-> Map<TipoDeChave, KeyType>
        private val mapping = KeyType.values().associateBy(KeyType::paraTipoDeChaveDoDominio)

        fun by(tipoDeChave: TipoDeChave) : KeyType{
            return mapping[tipoDeChave]
                ?: throw IllegalArgumentException("KeyType inválido ou não encontrado para $tipoDeChave")
        }
    }
}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {

    /**
     * https://open-banking.pass-consulting.com/json_ExternalCashAccountType1Code.html
     */
    enum class AccountType(val paraTipoContaDoDominio: TipoDeConta) {
        CACC(TipoDeConta.CORRENTE),
        SVGS(TipoDeConta.POUPANCA);

        companion object {
            fun by(tipoDeConta: TipoDeConta) : AccountType{
                return when(tipoDeConta){
                    TipoDeConta.CORRENTE -> CACC
                    TipoDeConta.POUPANCA -> SVGS
                }
            }
        }
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