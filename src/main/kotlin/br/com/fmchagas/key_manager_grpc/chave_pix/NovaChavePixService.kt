package br.com.fmchagas.key_manager_grpc.chave_pix

import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid


@Singleton
open class NovaChavePixService(
    @Inject val repository: ChavePixRepository
) {
    @Transactional
    open fun registrar(@Valid novaChavePix: NovaChavePix): ChavePix {

        if (novaChavePix.chavePix?.let { repository.existsByChavePix(it) } == true) {
            throw UnsupportedOperationException("chave pix '${novaChavePix.chavePix}' já cadastrada no sistema")
        }

        //TODO buscar dados da conta no ERP

        val chave = novaChavePix.toModel()
        repository.save(chave)
        return chave
    }

}
