package br.com.fmchagas.key_manager_grpc.compartilhado.grpc

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.Metadata

interface ExceptionHandler<E: Exception> {

    /**
     * Lidar com a exceção e mapeá-la para StatusWithDetails
     */
    fun handle(e: E): StatusWithDetails

    /**
     * Verifica se esta instância pode lidar com a exceção especificada ou não
     */
    fun supports(e: Exception): Boolean

    /**
     * simples wrapper para status e metadados
     */
    data class StatusWithDetails(val status: Status, val metadata: Metadata = Metadata()){
        constructor(se: StatusRuntimeException) : this(se.status, se.trailers ?: Metadata())
        constructor(sp: com.google.rpc.Status) : this(StatusProto.toStatusRuntimeException(sp))

        fun asRuntimeException(): StatusRuntimeException{
            return status.asRuntimeException()
        }
    }
}
