package br.com.fmchagas.key_manager_grpc.chave_pix

data class InformacaoDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {

    fun toModel() = Conta(agencia, numero, titular.nome, titular.cpf)
}

data class InstituicaoResponse(val nome: String, val ispb: String)
data class TitularResponse(val id: String, val nome: String, val cpf: String)