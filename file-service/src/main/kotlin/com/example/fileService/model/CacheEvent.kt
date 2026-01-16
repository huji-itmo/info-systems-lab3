package com.example.fileService.model

data class CacheEvent(
    val entityName: String,
    val entityId: Any?,
    val operation: String,
    val isCacheHit: Boolean,
    val executionTimeMs: Long,
    val methodName: String,
    val timestamp: Long = System.currentTimeMillis(),
)
