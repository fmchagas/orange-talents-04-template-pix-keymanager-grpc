package br.com.fmchagas.key_manager_grpc.chave_pix

import br.com.fmchagas.key_manager_grpc.util.ChavePixUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest{

    companion object{
        val TIPOS_DE_CHAVES_EXCETO_ALEATORIA = TipoDeChave.values().filterNot { it == TipoDeChave.CHAVE_ALEATORIA }
    }

    @Test
    fun `deve atualizar chave pix quando for do tipo aleatoria`(){
        // cenário
        val novaChave = ChavePixUtil.criaChavePixvalidaParametrizavel(TipoDeChave.CHAVE_ALEATORIA, UUID.randomUUID().toString())

        //ação
        val chaveAtualizada = "5260263c-a3c1-4727-ae32-3bdb2538841b"
        novaChave.atualizarChave(chaveAtualizada)

        // validação
        assertEquals(chaveAtualizada,novaChave.chavePix)
    }

    @Test
    fun `nao deve atualizar chave pix quando o tipo nao for aleatoria`(){
        // cenário
        val chavePix = "uma-chave-qualquer"
        var novaChave: ChavePix?

        //ação
        TIPOS_DE_CHAVES_EXCETO_ALEATORIA.forEach {
            novaChave = ChavePixUtil.criaChavePixvalidaParametrizavel(tipo = it, chavePix = chavePix)
            novaChave?.atualizarChave("5260263c-a3c1-4727-ae32-3bdb2538841b")
            novaChave?.id = 10L

            //validação
            assertEquals(chavePix, novaChave?.chavePix)
            assertEquals(it, novaChave?.tipoChave)
            assertNotNull(novaChave?.clienteId)
            assertNotNull(novaChave?.id)
            assertNotNull(novaChave?.conta)
            assertNotNull(novaChave?.tipoConta)
        }
    }
}