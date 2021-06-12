package br.com.fmchagas.key_manager_grpc.chave_pix

import java.util.regex.Pattern

enum class TipoDeChave {

    CHAVE_ALEATORIA{
        override fun valida(chave: String?)= chave.isNullOrEmpty() //não deve se preenchida
    },
    CPF {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrEmpty() || !chave.matches("[0-9]{11}".toRegex())){
                return false
            }

            /*if(!Pattern.compile("[0-9]{11}").matcher(chave).matches()){
                return false
            }*/
            //TODO usar beanvalidation para validar cpf
            return true
        }
    },
    TEL_CELULAR{
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrEmpty()){
                return false
            }

            return chave.matches("^\\+[1-9][0-9]\\d{1,14}$".toRegex())
        }
    },
    EMAIL{
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrEmpty()){
                return false
            }

            return Pattern.compile(
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
            ).matcher(chave).matches()
        }

    };

    abstract fun valida(chave: String?): Boolean
}