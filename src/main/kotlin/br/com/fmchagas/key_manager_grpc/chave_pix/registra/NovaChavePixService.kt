package br.com.fmchagas.key_manager_grpc.chave_pix.registra

import br.com.fmchagas.key_manager_grpc.chave_pix.*
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.*
import br.com.fmchagas.key_manager_grpc.compartilhado.exception.ChavePixExistenteException
import br.com.fmchagas.key_manager_grpc.compartilhado.exception.NotFoundException
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpResponse
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
    @Transactional
    open fun registrar(@Valid novaChavePix: NovaChavePix): ChavePix {

        if (novaChavePix.chavePix?.let { repository.existsByChavePix(it) } == true) {
            throw ChavePixExistenteException("chave pix '${novaChavePix.chavePix}' já cadastrada no sistema")
        }

        val response = clientItauERP.buscaViaHttp(novaChavePix.clienteId!!, "CONTA_" + novaChavePix.tipoConta!!.name)
        val conta: Conta = response.body()?.toModel() ?: throw NotFoundException("Conta do cliente não encontrada")

        val chave = novaChavePix.toModel(conta)
        repository.save(chave)

        val bcbResponse = registraChavePixNoBancoCentral(clientBcb, response.body(), chave)
        if(bcbResponse.status!= HttpStatus.CREATED){
            throw IllegalStateException("não foi possivel criar chave pix no banco central")
        }

        chave.atualizarChave(bcbResponse.body()!!.key)

        return chave
    }

    private fun registraChavePixNoBancoCentral(clientBcb: BcbClient,
                                               response: InformacaoDaContaResponse,
                                               chave : ChavePix
    ): HttpResponse<CreatePixKeyResponse?> {
        return clientBcb.registrarViaHttp(
            CreatePixKeyRequest(
                key = chave.chavePix,
                keyType = when(chave.tipoChave){
                    TipoDeChave.CPF -> KeyType.CPF
                    TipoDeChave.EMAIL -> KeyType.EMAIL
                    TipoDeChave.TEL_CELULAR -> KeyType.PHONE
                    TipoDeChave.CHAVE_ALEATORIA -> KeyType.RANDOM
                },
                bankAccount = BankAccount(
                    participant = response.instituicao.ispb, //ispb itau 60701190
                    branch = "0001",
                    accountNumber = chave.conta.numero,
                    accountType = when(chave.tipoConta){
                        TipoDeConta.CORRENTE -> BankAccount.AccountType.CACC
                        TipoDeConta.POUPANCA -> BankAccount.AccountType.SVGS
                    }
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = chave.conta.titularNome,
                    taxIdNumber = chave.conta.titularCpf
                )
            )
        )
    }

}
