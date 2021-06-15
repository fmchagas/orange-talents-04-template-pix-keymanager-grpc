package br.com.fmchagas.key_manager_grpc.compartilhado.grpc

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Around
@Type(ExceptionHandlerInterceptor::class)
annotation class ErrorHandler