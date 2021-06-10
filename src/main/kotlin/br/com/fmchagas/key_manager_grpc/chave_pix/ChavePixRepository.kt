package br.com.fmchagas.key_manager_grpc.chave_pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface ChavePixRepository : CrudRepository<ChavePix, Long> {
    abstract fun existsByChavePix(chaveDoPix: String): Boolean
}