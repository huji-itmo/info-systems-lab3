package com.example.fileService.service

import io.micrometer.core.instrument.MeterRegistry
import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import com.example.fileService.model.CacheStatistics as FileServiceCacheStatistics

@Service
class CacheStatisticsCollector(
    private val entityManager: EntityManager,
) {
    private val logger = LoggerFactory.getLogger(CacheStatisticsCollector::class.java)

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    fun collectAndReportCacheStatistics() {
        try {
            val sessionFactory = entityManager.unwrap(Session::class.java).sessionFactory
            val stats = sessionFactory.statistics

            val l2Hits = stats.secondLevelCacheHitCount
            val l2Misses = stats.secondLevelCacheMissCount
            val l2Puts = stats.secondLevelCachePutCount

            val hitRate =
                if (l2Hits + l2Misses > 0) {
                    (l2Hits.toDouble() / (l2Hits + l2Misses) * 100)
                } else {
                    0.0
                }

            logger.info(
                "CACHE_STATS - HitRate: {:.1f}%, L2Hits: {}, L2Misses: {}, L2Puts: {}",
                hitRate,
                l2Hits,
                l2Misses,
                l2Puts,
            )
        } catch (e: Exception) {
            logger.error("Failed to collect cache statistics", e)
        }
    }
}
