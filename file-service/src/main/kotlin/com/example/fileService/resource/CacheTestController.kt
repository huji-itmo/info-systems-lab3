package com.example.fileService.resource

import com.example.fileService.service.CacheLoggingTestService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test/cache")
class CacheTestController(
    private val cacheLoggingTestService: CacheLoggingTestService,
) {
    @GetMapping("/demo")
    fun demoCacheLogging(): ResponseEntity<Map<String, Any>> {
        try {
            cacheLoggingTestService.testCacheLogging()
            return ResponseEntity.ok(
                mapOf(
                    "status" to "success",
                    "message" to "Cache logging demo completed. Check logs for cache hit/miss information.",
                ),
            )
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(
                mapOf(
                    "status" to "error",
                    "message" to "Cache logging demo failed: ${e.message}",
                ),
            )
        }
    }
}
