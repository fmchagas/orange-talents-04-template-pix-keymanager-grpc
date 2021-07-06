package br.com.fmchagas.key_manager_grpc.compartilhado.grpc

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@MustBeDocumented
@Target(CLASS, TYPE, FILE, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Retention(RUNTIME)
@Around
@Type(ExceptionHandlerInterceptor::class)
annotation class ErrorHandler