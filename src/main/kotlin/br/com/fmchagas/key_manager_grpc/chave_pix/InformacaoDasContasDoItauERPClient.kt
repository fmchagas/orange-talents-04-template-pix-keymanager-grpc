package br.com.fmchagas.key_manager_grpc.chave_pix

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.annotation.Client

@Client("\${erp.itau.contas.url}")
interface InformacaoDasContasDoItauERPClient {

    @Get(uri = "/{clienteId}/contas")
    fun buscaViaHttp(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<InformacaoDaContaResponse>

}

data class InformacaoDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: Titular
) {

    fun toModel() = Conta(agencia, numero, titular.nome, titular.cpf)
}

data class InstituicaoResponse(val nome: String, val ispb: String)
data class Titular(val id: String, val nome: String, val cpf: String)