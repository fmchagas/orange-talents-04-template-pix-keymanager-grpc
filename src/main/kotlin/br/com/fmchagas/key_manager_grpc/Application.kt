package br.com.fmchagas.key_manager_grpc

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.fmchagas.key_manager_grpc")
		.start()
}

