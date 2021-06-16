package br.com.fmchagas.key_manager_grpc.chave_pix.registra

import br.com.fmchagas.key_manager_grpc.chave_pix.Conta
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeChave
import br.com.fmchagas.key_manager_grpc.chave_pix.TipoDeConta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*

internal class NovaChavePixTest{
    @Test
    fun `deve retornar model com chavePix aleatoria em formato UUID quando tipo da chave for aleatoria`(){
        val conta = Conta(agencia = "1010", numero = "101011", titularNome = "Teste", titularCpf = "73007268010", instituicaoNome = "Banco X" , instituicaoIsb = "1023")

        val chavePix = NovaChavePix(
            clienteId = "ea691b01-4567-498b-83b7-1552df6cb1f4",
            tipoChave = TipoDeChave.CHAVE_ALEATORIA,
            chavePix = "",
            tipoConta = TipoDeConta.POUPANCA,
        ).toModel(conta)

        val chavePixGerada = chavePix.chavePix

        assertNotNull(chavePix)
        assertEquals(TipoDeChave.CHAVE_ALEATORIA, chavePix.tipoChave)
        assertDoesNotThrow { UUID.fromString(chavePixGerada) }
    }

    @Test
    fun `deve retornar model com chavePix inforamda`(){
        val conta = Conta(agencia = "1010", numero = "101011", titularNome = "Teste", titularCpf = "73007268010", instituicaoNome = "Banco X" , instituicaoIsb = "1023")

        val chavePix = NovaChavePix(
            clienteId = "ea691b01-4567-498b-83b7-1552df6cb1f4",
            tipoChave = TipoDeChave.EMAIL,
            chavePix = "dunha@zup.com.br",
            tipoConta = TipoDeConta.CORRENTE,
        ).toModel(conta)

        assertNotNull(chavePix)
        assertEquals(TipoDeChave.EMAIL, chavePix.tipoChave)
        assertEquals(chavePix.chavePix, "dunha@zup.com.br")
    }
}