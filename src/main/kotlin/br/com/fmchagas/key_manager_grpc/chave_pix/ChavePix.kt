package br.com.fmchagas.key_manager_grpc.chave_pix


import java.time.Instant
import java.util.*
import javax.persistence.*
import javax.persistence.GenerationType.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull @Column(nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoDeChave,

    @field:NotEmpty
    @Column(nullable = false, length = 77, unique = true)
    var chavePix: String,

    @field:NotNull
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoDeConta,

    @field:NotNull
    @Embedded
    val conta: Conta
) {
    fun atualizarChave(chavePix: String) {
        this.chavePix = chavePix
    }

    @Id @GeneratedValue(strategy = IDENTITY)
    var id : Long? = null

    @NotNull
    @Column(nullable = false)
    val pixId = UUID.randomUUID()

    @NotNull
    @Column(nullable = false)
    val criadoEm = Instant.now()
}
