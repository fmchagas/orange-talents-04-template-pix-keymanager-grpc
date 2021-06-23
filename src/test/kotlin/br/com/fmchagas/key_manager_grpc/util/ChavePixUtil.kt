package br.com.fmchagas.key_manager_grpc.util

import br.com.fmchagas.key_manager_grpc.chave_pix.ChavePix
import br.com.fmchagas.key_manager_grpc.chave_pix.Conta
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeChave
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeConta
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.*
import java.time.LocalDateTime
import java.util.*

class ChavePixUtil{

    companion object{
        val NOME = "Fernando"
        val TIPO_CONTA = "CONTA_CORRENTE"
        val CLIENTE_ID = "5260263c-a3c1-4727-ae32-3bdb2538841b"
        val CHAVE = "fernando@gmail.com"

        fun informacaoDaContaResponse() = InformacaoDaContaResponse(
            tipo = TIPO_CONTA,
            instituicao = InstituicaoResponse("UNIBANCO ITAU", "60701190"),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse(CLIENTE_ID, NOME, "63657520325")
        )

        fun request() = CreatePixKeyRequest(
            keyType = KeyType.EMAIL,
            key = CHAVE,
            bankAccount = bankAccount(),
            owner = owner()
        )

        fun createPixKeyResponse() = CreatePixKeyResponse(
            keyType = KeyType.EMAIL,
            key = CHAVE,
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )

        fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
            return PixKeyDetailsResponse(
                keyType = KeyType.EMAIL,
                key = CHAVE,
                bankAccount = bankAccount(),
                owner = owner(),
                createdAt = LocalDateTime.now()
            )
        }

        fun criaChavePixvalida() = ChavePix(
            clienteId = UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
            tipoChave = TipoDeChave.CPF,
            chavePix = "73007268010",
            tipoConta = TipoDeConta.CORRENTE,
            Conta(
                agencia = "1010",
                numero = "101011",
                titularNome = "Teste",
                titularCpf = "73007268010",
                instituicaoNome = "UNIBANCO ITAU",
                instituicaoIsb = "1010"
            )
        )

        private fun bankAccount() = BankAccount(
            participant = "60701190",
            branch = "1218",
            accountNumber = "291900",
            accountType = BankAccount.AccountType.CACC
        )

        private fun owner() = Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = NOME,
            taxIdNumber = "63657520325"
        )
    }
}