package br.com.fmchagas.key_manager_grpc.chave_pix.registra

import br.com.fmchagas.key_manager_grpc.chave_pix.*
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.InformacaoDasContasDoItauERPClient
import br.com.fmchagas.key_manager_grpc.compartilhado.exception.ChavePixExistenteException
import br.com.fmchagas.key_manager_grpc.compartilhado.exception.NotFoundException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid


@Singleton
open class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val clientItauERP : InformacaoDasContasDoItauERPClient
) {
    @Transactional
    open fun registrar(@Valid novaChavePix: NovaChavePix): ChavePix {

        if (novaChavePix.chavePix?.let { repository.existsByChavePix(it) } == true) {
            throw ChavePixExistenteException("chave pix '${novaChavePix.chavePix}' já cadastrada no sistema")
        }

        val response = clientItauERP.buscaViaHttp(novaChavePix.clienteId!!, "CONTA_"+novaChavePix.tipoConta!!.name)

        val conta: Conta = response.body()?.toModel() ?: throw NotFoundException("Conta do cliente não encontrada")

        val chave = novaChavePix.toModel(conta)
        repository.save(chave)

        return chave
    }

}
