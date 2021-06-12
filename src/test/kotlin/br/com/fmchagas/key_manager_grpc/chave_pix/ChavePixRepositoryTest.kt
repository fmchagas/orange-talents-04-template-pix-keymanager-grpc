package br.com.fmchagas.key_manager_grpc.chave_pix

import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Inject

@MicronautTest(
    rollback = true, //padrão true
    transactionMode = TransactionMode.SINGLE_TRANSACTION, // SEPARATE_TRANSACTIONS é padrão
    transactional = false // padrão true - usando para que threads participem da transação
)
internal class ChavePixRepositoryTest {
    @Inject
    lateinit var chavePixRepository: ChavePixRepository

    @BeforeEach
    internal fun setup() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve retornar true quando existir chave pix`(){
        //senario
        chavePixRepository.save(
            ChavePix(
                UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
                TipoDeChave.CPF, "73007268010", TipoDeConta.CORRENTE
            )
        )

        // ação
        val existeChave = chavePixRepository.existsByChavePix("73007268010")

        assertTrue(existeChave)
    }

    @Test
    fun `deve retornar false quando nao existir chave pix`(){
        // ação
        val naoExisteChave = chavePixRepository.existsByChavePix("73007268010")

        assertFalse(naoExisteChave)
    }
}