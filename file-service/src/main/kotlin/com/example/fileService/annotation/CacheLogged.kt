package com.example.fileService.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CacheLogged(
    val entityName: String = "",
    val logArgs: Boolean = false,
    val logResult: Boolean = false,
    val thresholdMs: Long = 100,
    val enableMetrics: Boolean = true,
)
