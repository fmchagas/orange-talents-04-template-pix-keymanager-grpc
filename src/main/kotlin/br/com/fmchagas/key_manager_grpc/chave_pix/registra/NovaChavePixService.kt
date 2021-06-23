package br.com.fmchagas.key_manager_grpc.chave_pix.registra

import br.com.fmchagas.key_manager_grpc.chave_pix.*
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.*
import br.com.fmchagas.key_manager_grpc.compartilhado.exception.ChavePixExistenteException
import br.com.fmchagas.key_manager_grpc.compartilhado.exception.NotFoundException
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpResponse
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid


@Singleton
open class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val clientItauERP: InformacaoDasContasDoItauERPClient,
    @Inject val clientBcb: BcbClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    open fun registrar(@Valid novaChavePix: NovaChavePix): ChavePix {

        if (novaChavePix.chavePix?.let { repository.existsByChavePix(it) } == true) {
            throw ChavePixExistenteException("chave pix '${novaChavePix.chavePix}' já cadastrada no sistema")
        }

        // busca dados no ERP do ITAU
        val response = clientItauERP.buscaViaHttp(novaChavePix.clienteId!!, "CONTA_" + novaChavePix.tipoConta!!.name)
        val conta: Conta = response.body()?.toModel() ?: throw NotFoundException("Conta do cliente não encontrada")

        val chave = novaChavePix.toModel(conta)
        repository.save(chave)

        // registra chave no BCB
        val bcbRequest = CreatePixKeyRequest.paraModeloBcb(chave).also {
            logger.info("Registrando chave Pix no Banco Central: $it")
        }

        val bcbResponse = clientBcb.registrarViaHttp(bcbRequest)
        if(bcbResponse.status!= HttpStatus.CREATED){
            throw IllegalStateException("não foi possivel criar chave pix no banco central")
        }

        chave.atualizarChave(bcbResponse.body()!!.key)

        return chave
    }
}
