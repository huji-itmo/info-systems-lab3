package com.example.fileService.model

data class CacheStatistics(
    val secondLevelHitCount: Long,
    val secondLevelMissCount: Long,
    val secondLevelPutCount: Long,
    val queryCacheHitCount: Long,
    val queryCacheMissCount: Long,
    val timestamp: Long = System.currentTimeMillis(),
) {
    val hitRate: Double
        get() =
            if (secondLevelHitCount + secondLevelMissCount > 0) {
                secondLevelHitCount.toDouble() / (secondLevelHitCount + secondLevelMissCount)
            } else {
                0.0
            }
}
