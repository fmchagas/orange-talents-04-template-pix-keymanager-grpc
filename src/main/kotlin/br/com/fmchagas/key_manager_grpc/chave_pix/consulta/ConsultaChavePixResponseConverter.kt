package br.com.fmchagas.key_manager_grpc.chave_pix.consulta

import br.com.fmchagas.key_manager_grpc.grpc.ConsultarChavePixResponseGrpc
import br.com.fmchagas.key_manager_grpc.grpc.TipoChave
import br.com.fmchagas.key_manager_grpc.grpc.TipoConta
import com.google.protobuf.Timestamp

class ConsultaChavePixResponseConverter {
    fun converte(chavePixInfo: ChavePixInfo): ConsultarChavePixResponseGrpc {
        return ConsultarChavePixResponseGrpc.newBuilder()
            .setClienteId(chavePixInfo.clienteId?.toString() ?: "")
            .setPixId(chavePixInfo.pixId?.toString() ?: "")
            .setChave(
                ConsultarChavePixResponseGrpc.ChavePixGrpc.newBuilder()
                    .setTipo(TipoChave.valueOf(chavePixInfo.tipo.name))
                    .setChavePix(chavePixInfo.chavePix)
                    .setConta(
                        ConsultarChavePixResponseGrpc.ChavePixGrpc.ContaGrpc.newBuilder()
                            .setTipo(TipoConta.valueOf(chavePixInfo.tipoDeConta.name))
                            .setInstituicao(chavePixInfo.conta.instituicaoNome)
                            .setTitular(chavePixInfo.conta.titularNome)
                            .setCpf(chavePixInfo.conta.titularCpf)
                            .setAgencia(chavePixInfo.conta.agencia)
                            .setConta(chavePixInfo.conta.numero)
                            .build()
                    )
                    .setCriadoEm(
                        Timestamp.newBuilder()
                            .setSeconds(chavePixInfo.criadoEm.epochSecond)
                            .setNanos(chavePixInfo.criadoEm.nano)
                            .build()
                    )
                    .build()
            )
            .build()
    }
}