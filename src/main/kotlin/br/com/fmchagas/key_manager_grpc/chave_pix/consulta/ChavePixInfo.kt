package br.com.fmchagas.key_manager_grpc.chave_pix.consulta

import br.com.fmchagas.key_manager_grpc.chave_pix.ChavePix
import br.com.fmchagas.key_manager_grpc.chave_pix.Conta
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeChave
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeConta
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipo: TipoDeChave,
    val chavePix: String,
    val tipoDeConta: TipoDeConta,
    val conta: Conta,
    val criadoEm: Instant = Instant.now()
) {
    companion object{
        fun of(chavePix: ChavePix) : ChavePixInfo{
            return ChavePixInfo(
                pixId = chavePix.pixId,
                clienteId = chavePix.clienteId,
                tipo = chavePix.tipoChave,
                chavePix = chavePix.chavePix,
                tipoDeConta = chavePix.tipoConta,
                conta = chavePix.conta,
                criadoEm = chavePix.criadoEm
            )
        }
    }
}