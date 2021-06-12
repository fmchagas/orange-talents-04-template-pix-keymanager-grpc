package br.com.fmchagas.key_manager_grpc.chave_pix

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Embeddable
class Conta(
    @field:NotEmpty
    @Column(nullable = false, length = 20, name = "conta_agencia")
    val agencia: String,

    @field:NotEmpty
    @Column(nullable = false, length = 20, name = "conta_numero")
    val numero: String,

    @field:NotEmpty
    @Column(nullable = false, length = 128, name = "conta_titular_nome")
    val titularNome: String,

    @field:NotEmpty
    @Column(nullable = false, length = 11, name = "conta_titular_cpf")
    val titularCpf: String
)