package br.com.fmchagas.key_manager_grpc.chave_pix

import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
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
    lateinit var repository: ChavePixRepository

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve retornar true quando existir chave pix`() {
        //senario
        repository.save(criaChavePixvalida())

        // ação
        val existeChave = repository.existsByChavePix(criaChavePixvalida().chavePix)

        assertTrue(existeChave)
    }

    @Test
    fun `deve retornar false quando nao existir chave pix`() {
        // ação
        val naoExisteChave = repository.existsByChavePix("73007268010")

        assertFalse(naoExisteChave)
    }

    @Test
    fun `deve retornar a chave pix existente quando buscar pelo pixId e clienteId`() {
        //senario
        val chavePixSalva = repository.save(criaChavePixvalida())

        // ação
        val chavePix = repository.findByPixIdAndClienteId(
            chavePixSalva.pixId, criaChavePixvalida().clienteId
        )

        with(chavePix){
            assertNotNull(chavePix)
            assertEquals(criaChavePixvalida().chavePix, this?.chavePix)
            assertEquals(criaChavePixvalida().conta.agencia, this?.conta?.agencia)
        }
    }

    @Test
    fun `deve retornar null quando chave pix nao existir`() {

        // ação
        val chavePix = repository.findByPixIdAndClienteId(
            UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
            UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b")
        )

        with(chavePix){
            assertNull(chavePix)
        }
    }

    fun criaChavePixvalida() = ChavePix(
        clienteId = UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
        tipoChave = TipoDeChave.CPF,
        chavePix = "73007268010",
        tipoConta = TipoDeConta.CORRENTE,
        Conta(agencia = "1010", numero = "101011", titularNome = "Teste", titularCpf = "73007268010", instituicaoNome = "Banco X" , instituicaoIsb = "1023")
    )
}