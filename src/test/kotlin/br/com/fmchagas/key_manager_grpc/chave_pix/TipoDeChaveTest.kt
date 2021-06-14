package br.com.fmchagas.key_manager_grpc.chave_pix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoDeChaveTest{

    @Nested
    inner class ALEATORIA{
        @Test
        fun `deve ser valido quando chave aleatoria for vazia ou nula`(){
            with(TipoDeChave.CHAVE_ALEATORIA){
                assertTrue(valida(""))
                assertTrue(valida(null))
            }
        }

        @Test
        fun `nao deve ser valido quando chave aleatoria ter um valor`(){
            with(TipoDeChave.CHAVE_ALEATORIA){
                assertFalse(valida("73007268010"))
                assertFalse(valida("valor qualquer"))
            }
        }
    }

    @Nested
    inner class CPF{
        @Test
        fun `deve ser valido quando CPF for um numero valido`(){
            with(TipoDeChave.CPF){
                assertTrue(valida("73007268010"))
            }
        }

        @Test
        fun `nao deve ser valido quando CPF for um numero invalido`(){
            with(TipoDeChave.CPF){
                assertFalse(valida("73007268011"))
            }
        }

        @Test
        fun `nao deve ser valido quando CPF for vazio ou nulo`(){
            with(TipoDeChave.CPF){
                assertFalse(valida(""))
                assertFalse(valida(null))
            }
        }
    }

    @Nested
    inner class TELCELULAR{
        @Test
        fun `deve ser valido quando for um numero de telefone bem formado`(){
            //por exemplo: +5585988714077 número informado pelo negócio

            with(TipoDeChave.TEL_CELULAR){
                assertTrue(valida("+5585988714077"))
            }
        }

        @Test
        fun `nao deve ser valido quando for um numero de telefone mal formado`(){
            with(TipoDeChave.TEL_CELULAR){
                assertFalse(valida("85988714077"))
                assertFalse(valida("+55(85)988714077"))
                assertFalse(valida("+558598871-4077"))
            }
        }

        @Test
        fun `nao deve ser valido quando o numero de telefone for vazio ou nulo`(){
            with(TipoDeChave.TEL_CELULAR){
                assertFalse(valida(""))
                assertFalse(valida(null))
            }
        }

    }

    @Nested
    inner class EMAIL{
        @Test
        fun `deve ser valido quando email for bem formado`(){
            with(TipoDeChave.EMAIL){
                assertTrue(valida("user@dominio.com"))
                assertTrue(valida("user@dominio.br"))
                //assertTrue(valida("?#$@email.com"))
                //assertTrue(valida("?#$@?#$"))
            }
        }

        @Test
        fun `nao deve ser valido quando email for mal formado`(){
            with(TipoDeChave.EMAIL){
                assertFalse(valida("user@"))
                assertFalse(valida("@dominio.com"))
                assertFalse(valida("user@dominio.com."))
            }
        }

        @Test
        fun `nao deve ser valido quando email for vazio ou nulo`(){
            with(TipoDeChave.EMAIL){
                assertFalse(valida(""))
                assertFalse(valida(null))
            }
        }
    }
}