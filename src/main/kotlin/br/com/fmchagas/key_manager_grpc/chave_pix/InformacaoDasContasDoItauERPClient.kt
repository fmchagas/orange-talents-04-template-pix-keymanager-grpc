package br.com.fmchagas.key_manager_grpc.chave_pix

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.annotation.Client

@Client("\${erp.itau.contas.url}")
interface InformacaoDasContasDoItauERPClient {

    @Get(uri = "/{clienteId}/contas?tipo={tipo}")
    fun buscaViaHttp(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<InformacaoDaContaResponse>
}