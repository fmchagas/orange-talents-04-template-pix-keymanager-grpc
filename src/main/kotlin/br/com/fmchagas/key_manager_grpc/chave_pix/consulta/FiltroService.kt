package br.com.fmchagas.key_manager_grpc.chave_pix.consulta

import br.com.fmchagas.key_manager_grpc.chave_pix.ChavePixRepository
import br.com.fmchagas.key_manager_grpc.chave_pix.clients.BcbClient
import br.com.fmchagas.key_manager_grpc.compartilhado.exception.NotFoundException
import br.com.fmchagas.key_manager_grpc.compartilhado.validator.ValidUUID
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

@Singleton
open class FiltroService(
    @Inject private val repository: ChavePixRepository,
    @Inject private val clientBcb: BcbClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    open fun buscarPorChavePix(@NotBlank @Size(max = 77) chavePix: String): ChavePixInfo {
        logger.info("Buscando por chave pix local")

        return repository.findByChavePix(chavePix)?.let { chavePix ->
            ChavePixInfo.of(chavePix)
        } ?: also {
            logger.info("Busca chave pix '$chavePix' no Banco Central")
        }.clientBcb.buscarViaHttp(chavePix)?.let { httpResponse ->
            if (httpResponse.status == HttpStatus.OK) {
                return@let httpResponse.body()
            }
            return@let null
        }?.let { pixKeyDetailsResponse ->
            pixKeyDetailsResponse.toChavePixInfo()
        } ?: throw NotFoundException("Chave pix não encontada")
    }

    open fun buscarPorPixIdEClienteId(
        @NotEmpty @ValidUUID clienteId: String,
        @NotEmpty @ValidUUID pixId: String
    ): ChavePixInfo {

        return repository.findByPixIdAndClienteId(// sempre pertence ao cliente
            pixId = UUID.fromString(pixId),
            clienteId = UUID.fromString(clienteId)
        )?.let {
            ChavePixInfo.of(it)
        } ?: throw NotFoundException("Chave pix não encontada ou não pertence ao cliente")
    }
}